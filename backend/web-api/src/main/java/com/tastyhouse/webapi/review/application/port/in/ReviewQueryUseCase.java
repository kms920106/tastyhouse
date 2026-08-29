package com.tastyhouse.webapi.review.application.port.in;

import java.util.Optional;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewBestListItemResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewCommentListResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewDetailResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewLatestListItemResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewLikeStatusResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewMemberListItemResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewProductResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewWriteInfoResponse;

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
