package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

public interface ProductOwnerQueryPort {

    Optional<ProductManagementDetailResult> findProductManagementDetailById(Long productId);

    List<ProductCategoryManagementResult> findProductCategoriesForManagement(Long shopId);

    List<ProductImageManagementResult> findProductImagesForManagement(Long productId);

    boolean existsProductInShop(Long productId, Long shopId);

    Optional<ProductVegetarianSettingResult> findVegetarianSetting(Long productId);

    Optional<ProductExposurePeriodResult> findExposurePeriod(Long productId);

    List<ProductExposureHourResult> findExposureHours(Long productId);

    List<ProductOwnerPriceView> findPrices(Long productId);

    Optional<ProductNutritionResult> findNutrition(Long productId);

    List<String> findAllergenTypes(Long productId);
}
