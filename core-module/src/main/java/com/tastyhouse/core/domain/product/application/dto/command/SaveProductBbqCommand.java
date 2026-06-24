package com.tastyhouse.core.domain.product.application.dto.command;

public record SaveProductBbqCommand(
    Long productId,
    Long bbqMenuId,
    Long bbqCategoryId,
    boolean optionsSynced
) {}
