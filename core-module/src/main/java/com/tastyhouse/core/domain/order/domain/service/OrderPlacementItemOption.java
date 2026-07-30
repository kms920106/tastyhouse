package com.tastyhouse.core.domain.order.domain.service;

/**
 * 주문 상품 라인에서 선택된 옵션 하나 — 옵션 그룹과 옵션 식별자 쌍.
 */
public record OrderPlacementItemOption(
    Long groupId,
    Long optionId
) {

    public static OrderPlacementItemOption of(Long groupId, Long optionId) {
        return new OrderPlacementItemOption(groupId, optionId);
    }
}
