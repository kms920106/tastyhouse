package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * product 읽기 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>완전 매핑 전환으로 <b>응용 계층이 읽기 계약을 소유</b>하고 infrastructure-module의
 * {@code ProductQueryDao}가 이를 구현한다. 소비 모듈은 이 인터페이스와 같은 패키지의 반환 DTO
 * ({@code *Result})·검색 조건({@code *SearchCondition})만 알며, QueryDSL도 어댑터의 존재도 알지 않는다.
 *
 * <p>메서드명·시그니처는 DAO의 기존 공개 표면을 그대로 전사한 것이다(챕터 04는 순수 소유권 이동이라
 * 조회 동작·wire 계약을 바꾸지 않는다).
 */
public interface ProductQueryPort {

    PageResult<TodayDiscountProductResult> findTodayDiscountProducts(PageQuery pageQuery);

    PageResult<SearchProductItemResult> searchByKeyword(String keyword, PageQuery pageQuery);

    ProductOptionsResult findProductOptions(Long productId);

    List<ProductBatchResult> findProductsBatch(List<ProductBatchItem> items);

    List<String> findProductImageUrls(Long productId);

    List<ShopProductItemResult> findShopProducts(Long shopId);

    PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, PageQuery pageQuery);

    Optional<ProductDetailResult> findProductDetailById(Long productId);

    List<ProductPriceResult> findProductPrices(Long productId);

    List<ProductPriceResult> findProductPricesByProductIds(List<Long> productIds);

    List<ProductPriceResult> findShopProductPrices(Long shopId);

    long countVisibleProducts(Long shopId);

    Optional<ProductManagementDetailResult> findProductManagementDetailById(Long productId);

    Optional<ProductNutritionResult> findNutrition(Long productId);

    List<String> findAllergenTypes(Long productId);

    List<ProductCategoryResult> findProductCategories(Long shopId);

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

    PageResult<ProductImageChangeRequestResult> findImageChangeRequestPage(ApprovalStatus status, PageQuery pageQuery);

    List<ProductVegetarianRequestResult> findVegetarianRequests(Long productId);

    PageResult<ProductVegetarianRequestResult> findVegetarianRequestPage(ApprovalStatus status, PageQuery pageQuery);

    PageResult<ProductRepresentativeRequestResult> findRepresentativeRequestPage(ApprovalStatus status, PageQuery pageQuery);

    List<PopularProductItemResult> findPopularProducts(Long shopId);

    Optional<ProductVegetarianSettingResult> findVegetarianSetting(Long productId);

    Optional<ProductExposurePeriodResult> findExposurePeriod(Long productId);

    Optional<ProductBbqSyncTargetResult> findFirstBbqSyncTarget();
}
