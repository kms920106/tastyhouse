package com.tastyhouse.application.product.port.out.write;

public record ProductBbqState(
    Long id,
    Long productId,
    Long bbqMenuId,
    Long bbqCategoryId,
    boolean optionsSynced
) {
}
