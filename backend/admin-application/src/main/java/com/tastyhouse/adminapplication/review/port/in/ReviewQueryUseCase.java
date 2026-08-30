package com.tastyhouse.adminapplication.review.port.in;

import java.util.List;

import com.tastyhouse.adminapplication.review.response.ReviewCommentListItemResponse;
import com.tastyhouse.adminapplication.review.response.ReviewListItemResponse;
import com.tastyhouse.adminapplication.review.response.ReviewManagementDetailResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 리뷰 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReviewQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ReviewQueryUseCase {

    PaginationResponse<ReviewListItemResponse> getReviews(
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
    );

    ReviewManagementDetailResponse getReview(Long id);

    List<ReviewCommentListItemResponse> getComments(Long id);
}
