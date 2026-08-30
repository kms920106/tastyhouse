package com.tastyhouse.webapplication.search.port.in;

import java.util.List;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.product.response.ProductSummaryResponse;
import com.tastyhouse.webapplication.search.response.SearchPopularKeywordResponse;
import com.tastyhouse.webapplication.search.response.SearchRecommendedKeywordResponse;
import com.tastyhouse.webapplication.search.response.SearchReviewListItemResponse;
import com.tastyhouse.webapplication.search.response.SearchShopListItemResponse;

/**
 * 통합 검색 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code SearchQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface SearchQueryUseCase {

    List<SearchPopularKeywordResponse> getPopularKeywords();

    List<SearchRecommendedKeywordResponse> getRecommendedKeywords();

    PaginationResponse<ProductSummaryResponse> searchMenus(String query, int page, int size);

    PaginationResponse<SearchReviewListItemResponse> searchReviews(String query, int page, int size);

    PaginationResponse<SearchShopListItemResponse> searchShopsPaged(String query, Long memberId, int page, int size);

    PaginationResponse<SearchShopListItemResponse> searchShopsPublic(String query, int page, int size);
}
