package com.tastyhouse.application.search.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.review.port.out.SearchReviewItemResult;
import com.tastyhouse.application.search.port.out.PopularKeywordResult;
import com.tastyhouse.application.search.port.out.RecommendedKeywordResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;

@WebApp
public interface SearchQueryUseCase {

    List<PopularKeywordResult> getPopularKeywords();

    List<RecommendedKeywordResult> getRecommendedKeywords();

    PageResult<SearchProductItemResult> searchMenus(String query, int page, int size);

    PageResult<SearchReviewItemResult> searchReviews(String query, int page, int size);

    PageResult<ShopBookmarkedItemResult> searchShopsPaged(String query, Long memberId, int page, int size);

    PageResult<ShopBookmarkedItemResult> searchShopsPublic(String query, int page, int size);
}
