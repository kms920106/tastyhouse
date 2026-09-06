package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ProductOwnerQueryPort {

    Optional<ProductManagementDetailResult> findProductManagementDetailById(Long productId);

    List<ProductCategoryManagementResult> findProductCategoriesForManagement(Long shopId);

    List<ProductOptionGroupManagementResult> findProductOptionGroupsForManagement(Long shopId);

    List<ProductOptionGroupLinkedProductResult> findLinkedProductsByOptionGroupId(Long optionGroupId);

    Map<Long, List<ProductOptionGroupLinkedProductResult>> findLinkedProductsByShop(Long shopId);

    List<ProductOptionGroupMergeCandidateResult> findOptionGroupMergeCandidates(Long shopId);

    Set<String> findOptionGroupMergeExcludedSignatures(Long shopId);

    List<ProductAvailabilityItemResult> findProductAvailability(ProductAvailabilitySearchCondition condition);

    List<ProductOptionAvailabilityGroupResult> findProductOptionAvailability(ProductAvailabilitySearchCondition condition);

    List<ProductImageManagementResult> findProductImagesForManagement(Long productId);

    boolean existsProductInShop(Long productId, Long shopId);

    List<ProductImageChangeRequestResult> findImageChangeRequests(Long productId);

    List<ProductVegetarianRequestResult> findVegetarianRequests(Long productId);

    Optional<ProductVegetarianSettingResult> findVegetarianSetting(Long productId);

    Optional<ProductExposurePeriodResult> findExposurePeriod(Long productId);

    Optional<ProductNutritionResult> findNutrition(Long productId);

    List<String> findAllergenTypes(Long productId);
}
