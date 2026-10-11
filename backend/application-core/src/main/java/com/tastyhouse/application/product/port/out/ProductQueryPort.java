package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

public interface ProductQueryPort {

    List<ProductPriceResult> findProductPrices(Long productId);

    List<ProductPriceResult> findProductPricesByProductIds(List<Long> productIds);

    List<ProductPriceResult> findShopProductPrices(Long shopId);

    long countVisibleProducts(Long shopId);

    List<String> findProductImageUrls(Long productId);

    Optional<ProductDetailResult> findProductDetailById(Long productId);

    List<ProductCategoryResult> findProductCategories(Long shopId);

    Optional<ProductNutritionResult> findNutrition(Long productId);

    List<String> findAllergenTypes(Long productId);
}
