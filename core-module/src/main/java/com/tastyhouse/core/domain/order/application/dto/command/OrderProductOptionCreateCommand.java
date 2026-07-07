package com.tastyhouse.core.domain.order.application.dto.command;

public record OrderProductOptionCreateCommand(
    Long groupId,
    Long optionId
) {

    public static OrderProductOptionCreateCommand of(Long groupId, Long optionId) {
        return new OrderProductOptionCreateCommand(groupId, optionId);
    }
}
