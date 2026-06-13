package com.tastyhouse.core.domain.order.application.dto.command;

public record CreateOrderProductOptionCommand(
    Long groupId,
    Long optionId
) {
}
