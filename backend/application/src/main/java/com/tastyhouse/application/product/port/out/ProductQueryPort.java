package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductQueryPort {

    PageResult<TodayDiscountProductResult> findTodayDiscountProducts(ProductExposureWindow window, PageQuery pageQuery);

    PageResult<SearchProductItemResult> searchByKeyword(String keyword, ProductExposureWindow window, PageQuery pageQuery);

    List<ProductBatchResult> findProductsBatch(List<ProductBatchItem> items);

    List<ShopProductItemResult> findShopProducts(Long shopId, ProductExposureWindow window);

    List<ProductPriceResult> findProductPrices(Long productId);

    List<ProductPriceResult> findProductPricesByProductIds(List<Long> productIds);

    List<ProductPriceResult> findShopProductPrices(Long shopId);

    long countVisibleProducts(Long shopId);

    List<PopularProductItemResult> findPopularProducts(Long shopId, String soldOrderStatus, ProductExposureWindow window);

    ProductOptionsResult findProductOptions(Long productId, String commonOptionGroupType);

    List<String> findProductImageUrls(Long productId);

    Optional<ProductDetailResult> findProductDetailById(Long productId);

    List<ProductCategoryResult> findProductCategories(Long shopId);

    Optional<ProductNutritionResult> findNutrition(Long productId);

    List<String> findAllergenTypes(Long productId);
}
