package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ProductQueryPort {

    PageResult<TodayDiscountProductResult> findTodayDiscountProducts(PageQuery pageQuery);

    PageResult<SearchProductItemResult> searchByKeyword(String keyword, PageQuery pageQuery);

    List<ProductBatchResult> findProductsBatch(List<ProductBatchItem> items);

    List<ShopProductItemResult> findShopProducts(Long shopId);

    List<ProductPriceResult> findProductPrices(Long productId);

    List<ProductPriceResult> findProductPricesByProductIds(List<Long> productIds);

    List<ProductPriceResult> findShopProductPrices(Long shopId);

    long countVisibleProducts(Long shopId);

    List<PopularProductItemResult> findPopularProducts(Long shopId);

    ProductOptionsResult findProductOptions(Long productId);

    List<String> findProductImageUrls(Long productId);

    Optional<ProductDetailResult> findProductDetailById(Long productId);

    List<ProductCategoryResult> findProductCategories(Long shopId);

    Optional<ProductNutritionResult> findNutrition(Long productId);

    List<String> findAllergenTypes(Long productId);
}
