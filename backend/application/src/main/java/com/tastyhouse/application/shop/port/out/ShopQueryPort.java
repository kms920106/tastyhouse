package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

/**
 * 가게 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>가게 상세와 노출용 분류·이미지 조회를 담당한다. 여러 앱이 함께 보는 가게 기본 정보는
 * {@link ShopBasicInfoQueryPort}, 관리 화면 조회는 {@code ShopManagementQueryPort}(관리자)·
 * {@code ShopOwnerQueryPort}(점주)가 소유한다.
 *
 * <p>분할 전 이 포트는 35개 메서드에 세 앱의 조회가 모두 섞여 있었다(단독 18 · 공유 13). 메서드명이
 * {@code findVisible*}·{@code findExposed*}로 시작하는 데서 보이듯 이 포트에 남은 것은 <b>노출 여부를
 * 판정하는 회원 화면 전용</b> 조회다.
 */
public interface ShopQueryPort {

    List<ShopFoodTypeCategoryResult> findVisibleFoodTypeCategories();

    List<ShopAmenityCategoryResult> findVisibleAmenityCategories();

    List<ShopAmenityWithCategoryResult> findAmenitiesWithCategory(Long shopId);

    List<ShopMenuCollectionImageExposureResult> findExposedMenuCollectionImages(Long shopId);

    List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages();

    Optional<ShopVisibleDetailResult> findVisibleDetailById(Long shopId);

    boolean existsBookmark(Long shopId, Long memberId);
}
