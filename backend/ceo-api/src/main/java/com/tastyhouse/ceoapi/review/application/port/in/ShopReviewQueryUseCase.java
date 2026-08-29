package com.tastyhouse.ceoapi.review.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ReviewBlindReasonCatalogResponse;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ShopReviewDetailResponse;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ShopReviewListItemResponse;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ShopReviewSortTypeResponse;
import com.tastyhouse.ceoapi.review.adapter.in.web.response.ShopReviewStatisticsResponse;

/**
 * 가게 리뷰 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopReviewQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopReviewQueryUseCase {

    PaginationResponse<ShopReviewListItemResponse> getReviews(
        Long ceoId,
        Long shopId,
        String tab,
        LocalDate startDate,
        LocalDate endDate,
        Integer rating,
        String orderMethod,
        Boolean hasImage,
        String sortType,
        int page,
        int size
    );

    ShopReviewDetailResponse getReviewDetail(Long ceoId, Long shopId, Long reviewId);

    ShopReviewStatisticsResponse getStatistics(Long ceoId, Long shopId);

    ShopReviewSortTypeResponse getSortType(Long ceoId, Long shopId);

    List<ReviewBlindReasonCatalogResponse> getBlindReasons();
}
