package com.tastyhouse.core.domain.order.application.dto.command;

public record CreateOrderItemOptionCommand(
    Long groupId,
    Long optionId
) {
}
