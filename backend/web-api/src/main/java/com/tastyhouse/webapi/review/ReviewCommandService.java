package com.tastyhouse.webapi.review;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.repository.ReviewCommentRepository;
import com.tastyhouse.domain.review.repository.ReviewReplyRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.service.ReviewLifecycleService;
import com.tastyhouse.domain.review.service.ReviewRegistration;
import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
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
     * <p>{@code ownerOnly}(사장님만보기)는 박싱 {@code Boolean}으로 받아 {@code Boolean.TRUE.equals}로
     * 정규화한다 — 기존 클라이언트가 이 필드를 보내지 않으면 {@code null}이 오는데, 그때 공개(false)로
     * 동작해야 하기 때문이다(하위호환). 등록 시에만 정할 수 있고 이후 전환은 불가능하다.
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
        List<String> tags,
        Boolean ownerOnly
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
            tags,
            Boolean.TRUE.equals(ownerOnly)
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
     * 답글이 달릴 댓글의 상위 리뷰 식별자 — 컨트롤러가 가시성 가드를 걸 대상을 얻는 데 쓴다.
     *
     * <p>답글 경로는 {@code commentId}만 받아 리뷰를 알 수 없으므로, 이 조회 없이는 보이지 않는 리뷰의
     * 댓글 스레드에 답글을 붙일 수 있다. 댓글이 없으면 404.
     */
    public Long findReviewIdOfComment(Long commentId) {
        return reviewCommentRepository.findById(ReviewCommentId.of(commentId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_COMMENT_NOT_FOUND))
            .getReviewId()
            .value();
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
