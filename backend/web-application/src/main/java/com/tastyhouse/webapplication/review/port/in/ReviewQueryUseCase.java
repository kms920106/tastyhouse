package com.tastyhouse.webapplication.review.port.in;

import java.util.Optional;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.review.response.ReviewBestListItemResponse;
import com.tastyhouse.webapplication.review.response.ReviewCommentListResponse;
import com.tastyhouse.webapplication.review.response.ReviewDetailResponse;
import com.tastyhouse.webapplication.review.response.ReviewLatestListItemResponse;
import com.tastyhouse.webapplication.review.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapplication.review.response.ReviewMemberListItemResponse;
import com.tastyhouse.webapplication.review.response.ReviewProductResponse;
import com.tastyhouse.webapplication.review.response.ReviewResponse;
import com.tastyhouse.webapplication.review.response.ReviewWriteInfoResponse;

/**
 * 리뷰 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReviewQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ReviewQueryUseCase {

    PaginationResponse<ReviewBestListItemResponse> searchBestReviewList(int page, int size);

    PaginationResponse<ReviewLatestListItemResponse> searchLatestReviewList(int page, int size, String type, Long memberId);

    Optional<ReviewDetailResponse> findReviewDetail(Long reviewId, Long viewerMemberId);

    ReviewResponse getReviewResponse(Long reviewId, Long authorMemberId);

    ReviewLikeStatusResponse isLiked(Long reviewId, Long memberId);

    ReviewCommentListResponse searchCommentsWithReplies(Long reviewId, Long viewerMemberId);

    Optional<ReviewProductResponse> findReviewProduct(Long reviewId, Long viewerMemberId);

    ReviewWriteInfoResponse getReviewWriteInfo(Long orderProductId, Long memberId);

    PaginationResponse<ReviewMemberListItemResponse> findMemberReviews(Long memberId, int page, int size);

    void requireVisibleReview(Long reviewId, Long viewerMemberId);
}
