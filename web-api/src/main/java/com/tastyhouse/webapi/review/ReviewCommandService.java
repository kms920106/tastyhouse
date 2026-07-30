package com.tastyhouse.webapi.review;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;
import com.tastyhouse.core.domain.review.domain.model.Review;
import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.repository.ReviewCommentRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewReplyRepository;
import com.tastyhouse.core.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.core.domain.review.domain.service.ReviewLifecycleService;
import com.tastyhouse.core.domain.review.domain.service.ReviewRegistration;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.infrastructure.member.query.MemberQueryDao;
import com.tastyhouse.infrastructure.member.query.MemberWithProfileImageResult;
import com.tastyhouse.infrastructure.product.query.ProductDetailResult;
import com.tastyhouse.webapi.file.FileService;
import com.tastyhouse.webapi.product.ProductQueryService;
import com.tastyhouse.webapi.review.request.ReviewCreateRequest;
import com.tastyhouse.webapi.review.request.ReviewUpdateRequest;
import com.tastyhouse.webapi.review.response.ReviewCommentResponse;
import com.tastyhouse.webapi.review.response.ReviewReplyResponse;
import com.tastyhouse.webapi.review.response.ReviewResponse;

/**
 * 리뷰 명령 서비스(web).
 *
 * <p>리뷰 본문·이미지·태그를 함께 다루는 크로스 애그리거트 불변식은 도메인 서비스
 * {@link ReviewLifecycleService}가 갖고, 이 서비스는 트랜잭션 경계 선언과 HTTP 경계 타입
 * ({@code Long} 식별자 · Request/Response record) 변환만 담당한다.
 *
 * <p>댓글·답글 등록은 단일 애그리거트 저장이라 도메인 서비스를 거치지 않고 write 포트를 직접 호출한다.
 *
 * <p>조회 전용 동작은 {@link ReviewQueryService}로 분리했다(CQRS).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewCommandService {

    private final ReviewLifecycleService reviewLifecycleService;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ProductQueryService productQueryService;
    private final OrderQueryService orderQueryService;
    private final MemberQueryDao memberQueryDao;
    private final FileService fileService;
    private final FileQueryService fileQueryService;

    /**
     * 리뷰 등록 — 주문 상품이 지정되면 그 주문을 인증 근거로 함께 남긴다. 가게는 상품에서 역으로 얻는다.
     */
    public ReviewResponse createReview(Long memberId, ReviewCreateRequest request) {
        Long orderId = null;
        if (request.orderProductId() != null) {
            OrderProduct orderProduct = orderQueryService.findOrderProductById(OrderProductId.of(request.orderProductId()));
            orderId = orderProduct.getOrderId();
        }

        ProductDetailResult product = productQueryService.findProductDetail(request.productId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));

        ReviewRegistration registration = reviewLifecycleService.register(
            product.shopId(),
            product.id(),
            MemberId.of(memberId),
            orderId,
            request.tasteRating(),
            request.amountRating(),
            request.priceRating(),
            request.content(),
            request.uploadedFileIds(),
            request.tags()
        );

        return toReviewResponse(registration);
    }

    /**
     * 리뷰 수정 — 본인 리뷰만 수정할 수 있다(소유권 검증은 도메인 서비스가 수행).
     */
    public ReviewResponse updateReview(Long reviewId, Long memberId, ReviewUpdateRequest request) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        ReviewRegistration registration = reviewLifecycleService.modify(
            targetReviewId,
            MemberId.of(memberId),
            request.tasteRating(),
            request.amountRating(),
            request.priceRating(),
            request.content(),
            request.uploadedFileIds(),
            request.tags()
        );

        return toReviewResponse(registration);
    }

    /**
     * 리뷰 삭제(본인) — 삭제 이벤트에 실을 상품 식별자를 삭제 전 리뷰에서 읽어 넘긴다.
     */
    public void deleteReview(Long reviewId, Long memberId) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        Review review = reviewRepository.findById(targetReviewId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

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
     */
    public ReviewCommentResponse createComment(Long reviewId, Long memberId, String content) {
        ReviewComment comment = reviewCommentRepository.save(ReviewComment.of(reviewId, MemberId.of(memberId), content));

        MemberWithProfileImageResult member = memberQueryDao.findMemberWithProfileImagesByIds(List.of(memberId)).get(memberId);
        return toCommentResponse(comment, member, List.of());
    }

    /**
     * 댓글 답글 등록 — 답글 대상 회원은 없을 수 있다.
     */
    public ReviewReplyResponse createReply(Long commentId, Long memberId, Long replyToMemberId, String content) {
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        ReviewReply reply = reviewReplyRepository.save(ReviewReply.of(
            reviewCommentId.value(),
            MemberId.of(memberId),
            replyToMemberId == null ? null : MemberId.of(replyToMemberId),
            content
        ));

        List<Long> ids = replyToMemberId != null ? List.of(memberId, replyToMemberId) : List.of(memberId);
        Map<Long, MemberWithProfileImageResult> memberMap = memberQueryDao.findMemberWithProfileImagesByIds(ids);
        return toReplyResponse(reply, memberMap.get(memberId), replyToMemberId != null ? memberMap.get(replyToMemberId) : null);
    }

    private ReviewResponse toReviewResponse(ReviewRegistration registration) {
        Review review = registration.review();

        return ReviewResponse.from(
            review.getReviewId().value(),
            review.getProductId(),
            review.getTasteRating(),
            review.getAmountRating(),
            review.getPriceRating(),
            review.getTotalRating(),
            review.getContent(),
            toImageUrls(registration.uploadedFileIds()),
            registration.tags(),
            review.getCreatedAt()
        );
    }

    private ReviewCommentResponse toCommentResponse(
        ReviewComment comment,
        MemberWithProfileImageResult member,
        List<ReviewReplyResponse> replies
    ) {
        return ReviewCommentResponse.from(
            comment.getId(),
            comment.getReviewId(),
            comment.getMemberId().value(),
            member != null ? member.nickname() : null,
            member != null ? fileService.getUrlByPath(member.profileImageFilePath()) : null,
            comment.getContent(),
            comment.getCreatedAt(),
            replies
        );
    }

    private ReviewReplyResponse toReplyResponse(
        ReviewReply reply,
        MemberWithProfileImageResult member,
        MemberWithProfileImageResult replyToMember
    ) {
        return ReviewReplyResponse.from(
            reply.getId(),
            reply.getCommentId(),
            reply.getMemberId().value(),
            member != null ? member.nickname() : null,
            member != null ? fileService.getUrlByPath(member.profileImageFilePath()) : null,
            reply.getReplyToMemberId() != null ? reply.getReplyToMemberId().value() : null,
            replyToMember != null ? replyToMember.nickname() : null,
            reply.getContent(),
            reply.getCreatedAt()
        );
    }

    /**
     * 업로드 파일 식별자를 표시용 URL로 변환한다. 경로를 찾지 못한 항목은 제외한다.
     */
    private List<String> toImageUrls(List<Long> imageFileIds) {
        if (imageFileIds == null || imageFileIds.isEmpty()) {
            return List.of();
        }

        return imageFileIds.stream()
            .map(fileId -> fileQueryService.findFilePath(UploadedFileId.of(fileId))
                .map(fileService::getUrlByPath)
                .orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }
}
