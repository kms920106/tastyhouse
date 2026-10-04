package com.tastyhouse.domain.order.model;

public record OrderPlacementItemOption(
    Long groupId,
    Long optionId
) {

    public static OrderPlacementItemOption of(Long groupId, Long optionId) {
        return new OrderPlacementItemOption(groupId, optionId);
    }
}
