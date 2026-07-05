package com.tastyhouse.core.domain.order.application.dto.command;

public record CreateOrderProductOptionCommand(
    Long groupId,
    Long optionId
) {

    public static CreateOrderProductOptionCommand of(Long groupId, Long optionId) {
        return new CreateOrderProductOptionCommand(groupId, optionId);
    }
}
