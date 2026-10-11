package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopChangeHistoryResult(
    Long id,
    String category,
    String categoryDescription,
    String changeType,
    String changeTypeDescription,
    String actionType,
    String actionTypeDescription,
    String previousValue,
    String newValue,
    LocalDateTime changedAt
) {

    public ShopChangeHistoryResult withDescriptions(
        String categoryDescription,
        String changeTypeDescription,
        String actionTypeDescription
    ) {
        return new ShopChangeHistoryResult(
            this.id,
            this.category,
            categoryDescription,
            this.changeType,
            changeTypeDescription,
            this.actionType,
            actionTypeDescription,
            this.previousValue,
            this.newValue,
            this.changedAt
        );
    }
}
