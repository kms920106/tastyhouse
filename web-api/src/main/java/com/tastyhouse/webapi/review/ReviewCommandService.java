package com.tastyhouse.webapi.review;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.model.OrderProduct;
import com.tastyhouse.domain.order.domain.repository.OrderProductRepository;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.order.domain.vo.OrderProductId;
import com.tastyhouse.domain.product.domain.model.Product;
import com.tastyhouse.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.domain.product.domain.vo.ProductId;
import com.tastyhouse.domain.review.domain.model.Review;
import com.tastyhouse.domain.review.domain.model.ReviewComment;
import com.tastyhouse.domain.review.domain.model.ReviewReply;
import com.tastyhouse.domain.review.domain.repository.ReviewCommentRepository;
import com.tastyhouse.domain.review.domain.repository.ReviewReplyRepository;
import com.tastyhouse.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.domain.review.domain.service.ReviewLifecycleService;
import com.tastyhouse.domain.review.domain.service.ReviewRegistration;
import com.tastyhouse.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.domain.review.domain.vo.ReviewId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 리뷰 명령 서비스(web).
 *
 * <p>리뷰 본문·이미지·태그를 함께 다루는 크로스 애그리거트 불변식은 도메인 서비스
 * {@link ReviewLifecycleService}가 갖고, 이 서비스는 트랜잭션 경계 선언과 HTTP 경계 타입
 * ({@code Long} 식별자 · 원시 파라미터) 승격만 담당한다.
 *
 * <p>댓글·답글 등록은 단일 애그리거트 저장이라 도메인 서비스를 거치지 않고 write 포트를 직접 호출한다.
 *
 * <p><b>CQRS 교차 주입 금지</b> — 이 서비스는 infra query DAO({@code ..infrastructure..query..})도
 * 같은 모듈의 {@code *QueryService}도 주입하지 않는다. 그래서 (1) 모든 명령은 <b>식별자만</b> 반환하고
 * 응답 조립은 커밋 이후 컨트롤러가 {@link ReviewQueryService}로 재조회해 담당하며, (2) 리뷰 등록 시
 * 필요한 상품→가게 역조회는 표현용 투영({@code ProductQueryDao})이 아니라 write 포트
 * {@link ProductRepository#findById}의 정당한 단건 로드로 수행한다 — 이 값은 화면 표시용이 아니라
 * "리뷰가 어느 가게에 속하는가"를 확정하는 불변식 입력이기 때문이다.
 *
 * <p>조회 전용 동작은 {@link ReviewQueryService}로 분리했다(CQRS).
 */
@Service
@Transactional
public class ReviewCommandService {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;

    public ReviewCommandService(
        ReviewLifecycleService reviewLifecycleService,
        ReviewRepository reviewRepository,
        ReviewCommentRepository reviewCommentRepository,
        ReviewReplyRepository reviewReplyRepository,
        ProductRepository productRepository,
        OrderProductRepository orderProductRepository
    ) {
        this.reviewLifecycleService = reviewLifecycleService;
        this.reviewRepository = reviewRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewReplyRepository = reviewReplyRepository;
        this.productRepository = productRepository;
        this.orderProductRepository = orderProductRepository;
    }

    /**
     * 리뷰 등록 — 주문 상품이 지정되면 그 주문을 인증 근거로 함께 남긴다. 가게는 상품에서 역으로 얻는다.
     *
     * @return 등록된 리뷰 식별자
     */
    public Long createReview(
        Long memberId,
        Long orderProductId,
        Long productId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags
    ) {
        OrderId orderId = null;
        if (orderProductId != null) {
            OrderProduct orderProduct = orderProductRepository.findById(OrderProductId.of(orderProductId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_ORDER_PRODUCT_NOT_FOUND));
            orderId = orderProduct.getOrderId();
        }

        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        ReviewRegistration registration = reviewLifecycleService.register(
            product.getShopId(),
            product.getProductId(),
            MemberId.of(memberId),
            orderId,
            tasteRating,
            amountRating,
            priceRating,
            content,
            uploadedFileIds,
            tags
        );

        return registration.review().getReviewId().value();
    }

    /**
     * 리뷰 수정 — 본인 리뷰만 수정할 수 있다(소유권 검증은 도메인 서비스가 수행).
     *
     * @return 수정된 리뷰 식별자
     */
    public Long updateReview(
        Long reviewId,
        Long memberId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags
    ) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        ReviewRegistration registration = reviewLifecycleService.modify(
            targetReviewId,
            MemberId.of(memberId),
            tasteRating,
            amountRating,
            priceRating,
            content,
            uploadedFileIds,
            tags
        );

        return registration.review().getReviewId().value();
    }

    /**
     * 리뷰 삭제(본인) — 삭제 이벤트에 실을 상품 식별자를 삭제 전 리뷰에서 읽어 넘긴다.
     */
    public void deleteReview(Long reviewId, Long memberId) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        Review review = reviewRepository.findById(targetReviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        reviewLifecycleService.removeOwnedBy(targetReviewId, MemberId.of(memberId), review.getProductId());
    }

    /**
     * 리뷰 좋아요 토글 — 반환값은 토글 <b>후</b> 좋아요 상태다.
     */
    public boolean toggleReviewLike(Long reviewId, Long memberId) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        return reviewLifecycleService.toggleLike(targetReviewId, MemberId.of(memberId));
    }

    /**
     * 리뷰 댓글 등록.
     *
     * @return 등록된 댓글 식별자
     */
    public Long createComment(Long reviewId, Long memberId, String content) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        ReviewComment comment = reviewCommentRepository.save(
            ReviewComment.of(targetReviewId, MemberId.of(memberId), content)
        );
        return comment.getId();
    }

    /**
     * 댓글 답글 등록 — 답글 대상 회원은 없을 수 있다.
     *
     * @return 등록된 답글 식별자
     */
    public Long createReply(Long commentId, Long memberId, Long replyToMemberId, String content) {
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        ReviewReply reply = reviewReplyRepository.save(ReviewReply.of(
            reviewCommentId,
            MemberId.of(memberId),
            replyToMemberId == null ? null : MemberId.of(replyToMemberId),
            content
        ));

        return reply.getId();
    }
}
