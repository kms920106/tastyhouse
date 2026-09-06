package com.tastyhouse.domain.shop.service;

/**
 * 구간별 배달팁 replace-all 입력 한 행.
 *
 * <p>{@code tierOrder}를 담지 않는 것이 의도다 — 순서는 {@code ShopDeliveryTipService}가 정렬 후
 * {@code 0..n-1}로 재부여하므로, 호출부가 보낸 순서 값이 정렬 결과와 어긋나 조용히 틀어질 여지를 없앤다.
 */
public record ShopDeliveryTipTierSpec(int minOrderAmount, int tipAmount) {

    public static ShopDeliveryTipTierSpec of(int minOrderAmount, int tipAmount) {
        return new ShopDeliveryTipTierSpec(minOrderAmount, tipAmount);
    }
}
