package com.tastyhouse.adminapplication.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.review.port.out.ReviewCommentListItemResult;
import com.tastyhouse.application.review.port.out.ReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewManagementDetailResult;
import com.tastyhouse.application.review.port.out.ReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewTagQueryPort;
import com.tastyhouse.application.review.port.out.ReviewReplyListItemResult;
import com.tastyhouse.application.review.port.out.ReviewSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.review.response.ReviewCommentListItemResponse;
import com.tastyhouse.adminapplication.review.response.ReviewListItemResponse;
import com.tastyhouse.adminapplication.review.response.ReviewManagementDetailResponse;
import com.tastyhouse.adminapplication.review.response.ReviewReplyListItemResponse;
import com.tastyhouse.adminapplication.review.port.in.ReviewQueryUseCase;

/**
 * 리뷰 관리 조회 서비스(admin).
 *
 * <p>관리 화면은 숨김 리뷰·댓글·답글까지 모두 봐야 하므로 관리 전용 read 어댑터
 * ({@link ReviewManagementQueryPort})를 쓰고, 태그명처럼 web과 공유하는 조회만 {@link ReviewTagQueryPort}를
 * 쓴다. 파일 경로 → 표시용 URL 변환은 DAO가 담당하므로 이 계층은 이미 URL이 된 필드를 그대로 조립한다.
 *
 * <p>명령 동작은 {@link ReviewCommandService}로 분리했다(CQRS).
 */
@Service
@Transactional(readOnly = true)
public class ReviewQueryService implements ReviewQueryUseCase {

    private final ReviewManagementQueryPort reviewManagementQueryPort;
    private final ReviewTagQueryPort reviewTagQueryPort;

    public ReviewQueryService(ReviewManagementQueryPort reviewManagementQueryPort, ReviewTagQueryPort reviewTagQueryPort) {
        this.reviewManagementQueryPort = reviewManagementQueryPort;
        this.reviewTagQueryPort = reviewTagQueryPort;
    }

    /**
     * 리뷰 목록(숨김 포함) — 검색 조건으로 필터링한다.
     */
    @Override
    public PaginationResponse<ReviewListItemResponse> getReviews(
        Long shopId,
        Long productId,
        Long memberId,
        Boolean hidden,
        Boolean ownerOnly,
        String content,
        Double minRating,
        Double maxRating,
        int page,
        int size
    ) {
        ReviewSearchCondition condition = ReviewSearchCondition.of(shopId, productId, memberId, hidden, ownerOnly, content, minRating, maxRating);
        PageResult<ReviewListItemResponse> pageResult = reviewManagementQueryPort.findReviews(condition, PageQuery.of(page, size))
            .map(this::toReviewListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 리뷰 상세(숨김 포함) — 상세 본문 조회와 태그명 조회를 조합한다.
     */
    @Override
    public ReviewManagementDetailResponse getReview(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        ReviewManagementDetailResult detail = reviewManagementQueryPort.findReviewManagementDetail(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        List<Long> tagIds = reviewTagQueryPort.findTagIdsByReviewId(reviewId.value());
        if (!tagIds.isEmpty()) {
            detail = detail.withTagNames(reviewTagQueryPort.findTagNamesByIds(tagIds));
        }

        return toReviewManagementDetailResponse(detail);
    }

    /**
     * 리뷰의 댓글·답글 목록(숨김 포함).
     */
    @Override
    public List<ReviewCommentListItemResponse> getComments(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        List<ReviewCommentListItemResult> comments = reviewManagementQueryPort.findCommentsIncludingHidden(reviewId);

        List<ReviewCommentId> commentIds = comments.stream()
            .map(comment -> ReviewCommentId.of(comment.id()))
            .toList();
        List<ReviewReplyListItemResult> replies = reviewManagementQueryPort.findRepliesIncludingHidden(commentIds);

        return comments.stream()
            .map(comment -> toReviewCommentListItemResponse(comment, replies))
            .toList();
    }

    private ReviewListItemResponse toReviewListItemResponse(ReviewListItemResult dto) {
        return ReviewListItemResponse.from(
            dto.id(),
            dto.shopId(),
            dto.productId(),
            dto.memberId(),
            dto.memberNickname(),
            dto.totalRating(),
            dto.content(),
            dto.hidden(),
            dto.ownerOnly(),
            dto.createdAt()
        );
    }

    private ReviewManagementDetailResponse toReviewManagementDetailResponse(ReviewManagementDetailResult dto) {
        return ReviewManagementDetailResponse.from(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.stationName(),
            dto.content(),
            dto.totalRating(),
            dto.tasteRating(),
            dto.amountRating(),
            dto.priceRating(),
            dto.atmosphereRating(),
            dto.kindnessRating(),
            dto.hygieneRating(),
            dto.willRevisit(),
            dto.hidden(),
            dto.ownerOnly(),
            dto.memberId(),
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.createdAt(),
            dto.imageUrls(),
            dto.tagNames()
        );
    }

    private ReviewCommentListItemResponse toReviewCommentListItemResponse(
        ReviewCommentListItemResult comment,
        List<ReviewReplyListItemResult> replies
    ) {
        List<ReviewReplyListItemResponse> commentReplies = replies.stream()
            .filter(reply -> reply.commentId().equals(comment.id()))
            .map(this::toReviewReplyListItemResponse)
            .toList();

        return ReviewCommentListItemResponse.from(
            comment.id(),
            comment.memberId(),
            comment.memberNickname(),
            comment.content(),
            comment.hidden(),
            comment.createdAt(),
            commentReplies
        );
    }

    private ReviewReplyListItemResponse toReviewReplyListItemResponse(ReviewReplyListItemResult dto) {
        return ReviewReplyListItemResponse.from(
            dto.id(),
            dto.memberId(),
            dto.memberNickname(),
            dto.replyToMemberId(),
            dto.replyToMemberNickname(),
            dto.content(),
            dto.hidden(),
            dto.createdAt()
        );
    }
}
