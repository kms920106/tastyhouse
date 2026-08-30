package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 상품 관리 조회 포트(CQRS query 측 아웃바운드 포트) — 점주 관리 화면용.
 *
 * <p>점주가 자기 가게의 메뉴를 편집할 때 쓰는 조회를 담당한다 — 관리용 상세·분류·옵션그룹,
 * 옵션그룹 병합 후보, 품절 관리, 이미지·베지테리언 변경 요청 현황이다. 회원 화면 조회는
 * {@link ProductQueryPort}, 관리자 검수 화면 조회는 {@link ProductManagementQueryPort}가 소유한다.
 *
 * <p>관리자 계약이 {@code Management} 한정어를 이미 쓰고 있으므로(반환 타입도
 * {@code ProductListItemResult} 계열), 점주 관리 계약은 소유 주체를 담은 {@code Owner}로 구별한다.
 * 이 포트가 반환하는 {@code *ManagementResult} 계열은 점주 화면 투영이며 챕터 06에서
 * ceo-application으로 이동한다.
 */
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

    /** 공유 메서드 — {@link ProductQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ProductNutritionResult> findNutrition(Long productId);

    /** 공유 메서드 — {@link ProductQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<String> findAllergenTypes(Long productId);
}
