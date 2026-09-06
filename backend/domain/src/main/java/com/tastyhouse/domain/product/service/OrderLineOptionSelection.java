package com.tastyhouse.domain.product.service;

public record OrderLineOptionSelection(
    Long groupId,
    Long optionId
) {
    public static OrderLineOptionSelection of(Long groupId, Long optionId) {
        return new OrderLineOptionSelection(groupId, optionId);
    }
}
