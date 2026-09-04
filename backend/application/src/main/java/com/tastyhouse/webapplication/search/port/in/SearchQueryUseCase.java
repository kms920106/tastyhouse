package com.tastyhouse.webapplication.search.port.in;

import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.review.port.out.SearchReviewItemResult;
import com.tastyhouse.application.search.port.out.PopularKeywordResult;
import com.tastyhouse.application.search.port.out.RecommendedKeywordResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;

/**
 * 통합 검색 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code SearchQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 10</b>에서 메뉴 검색이 마지막으로 전환됐다 — product 컨텍스트의 Response 승격이 끝나며
 * {@code ProductSummaryResponse} 반환이 {@code PageResult<SearchProductItemResult>}로 바뀌었고, 이제 이
 * 포트에는 표현 계약이 하나도 남지 않았다.
 */
public interface SearchQueryUseCase {

    List<PopularKeywordResult> getPopularKeywords();

    List<RecommendedKeywordResult> getRecommendedKeywords();

    PageResult<SearchProductItemResult> searchMenus(String query, int page, int size);

    PageResult<SearchReviewItemResult> searchReviews(String query, int page, int size);

    PageResult<ShopBookmarkedItemResult> searchShopsPaged(String query, Long memberId, int page, int size);

    PageResult<ShopBookmarkedItemResult> searchShopsPublic(String query, int page, int size);
}
