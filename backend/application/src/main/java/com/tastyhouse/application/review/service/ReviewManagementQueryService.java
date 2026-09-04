package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.marker.AdminApp;
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
import com.tastyhouse.application.review.port.in.ReviewManagementQueryUseCase;

/**
 * 리뷰 관리 조회 서비스(admin).
 *
 * <p>관리 화면은 숨김 리뷰·댓글·답글까지 모두 봐야 하므로 관리 전용 read 어댑터
 * ({@link ReviewManagementQueryPort})를 쓰고, 태그명처럼 web과 공유하는 조회만 {@link ReviewTagQueryPort}를
 * 쓴다. 파일 경로 → 표시용 URL 변환은 DAO가 담당하므로 이 계층은 이미 URL이 된 필드를 그대로 넘긴다.
 *
 * <p>명령 동작은 {@link ReviewManagementCommandService}로 분리했다(CQRS).
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이며, 댓글에 답글을
 * 중첩하는 조립도 {@code ReviewCommentListItemResponse.from(...)}이 담당한다.
 */
@Service
@AdminApp
@Transactional(readOnly = true)
public class ReviewManagementQueryService implements ReviewManagementQueryUseCase {

    private final ReviewManagementQueryPort reviewManagementQueryPort;
    private final ReviewTagQueryPort reviewTagQueryPort;

    public ReviewManagementQueryService(ReviewManagementQueryPort reviewManagementQueryPort, ReviewTagQueryPort reviewTagQueryPort) {
        this.reviewManagementQueryPort = reviewManagementQueryPort;
        this.reviewTagQueryPort = reviewTagQueryPort;
    }

    /**
     * 리뷰 목록(숨김 포함) — 검색 조건으로 필터링한다.
     */
    @Override
    public PageResult<ReviewListItemResult> getReviews(
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
        return reviewManagementQueryPort.findReviews(condition, PageQuery.of(page, size));
    }

    /**
     * 리뷰 상세(숨김 포함) — 상세 본문 조회와 태그명 조회를 조합한다.
     */
    @Override
    public ReviewManagementDetailResult getReview(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        ReviewManagementDetailResult detail = reviewManagementQueryPort.findReviewManagementDetail(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        List<Long> tagIds = reviewTagQueryPort.findTagIdsByReviewId(reviewId.value());
        if (!tagIds.isEmpty()) {
            detail = detail.withTagNames(reviewTagQueryPort.findTagNamesByIds(tagIds));
        }

        return detail;
    }

    /**
     * 리뷰의 댓글 목록(숨김 포함).
     */
    @Override
    public List<ReviewCommentListItemResult> getComments(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        return reviewManagementQueryPort.findCommentsIncludingHidden(reviewId);
    }

    /**
     * 댓글들에 달린 답글 목록(숨김 포함) — 댓글별로 나눠 담는 것은 컨트롤러의 Response 조립이 한다.
     */
    @Override
    public List<ReviewReplyListItemResult> getReplies(List<ReviewCommentListItemResult> comments) {
        List<ReviewCommentId> commentIds = comments.stream()
            .map(comment -> ReviewCommentId.of(comment.id()))
            .toList();
        return reviewManagementQueryPort.findRepliesIncludingHidden(commentIds);
    }
}
