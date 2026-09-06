package com.tastyhouse.domain.shop.service;

/**
 * 지역별 배달팁 replace-all 입력 한 행.
 *
 * <p>{@code adminDongId}가 VO가 아니라 raw {@code Long}인 것은 HTTP 경계에서 넘어온 값을 그대로 받기
 * 위해서이며, VO 승격은 {@code ShopDeliveryTipService}가 검증과 함께 수행한다.
 */
public record ShopDeliveryTipRegionSpec(Long adminDongId, int tipAmount) {

    public static ShopDeliveryTipRegionSpec of(Long adminDongId, int tipAmount) {
        return new ShopDeliveryTipRegionSpec(adminDongId, tipAmount);
    }
}
