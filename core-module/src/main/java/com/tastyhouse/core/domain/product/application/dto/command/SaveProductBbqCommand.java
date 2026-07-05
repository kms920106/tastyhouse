package com.tastyhouse.core.domain.product.application.dto.command;

public record SaveProductBbqCommand(
    Long productId,
    Long bbqMenuId,
    Long bbqCategoryId,
    boolean optionsSynced
) {

    public static SaveProductBbqCommand of(
        Long productId,
        Long bbqMenuId,
        Long bbqCategoryId,
        boolean optionsSynced
    ) {
        return new SaveProductBbqCommand(productId, bbqMenuId, bbqCategoryId, optionsSynced);
    }
}
