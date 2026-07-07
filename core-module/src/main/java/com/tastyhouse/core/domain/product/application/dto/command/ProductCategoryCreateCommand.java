package com.tastyhouse.core.domain.product.application.dto.command;

public record ProductCategoryCreateCommand(
    Long shopId,
    String name,
    Integer sort,
    boolean visible
) {

    public static ProductCategoryCreateCommand of(Long shopId, String name, Integer sort, boolean visible) {
        return new ProductCategoryCreateCommand(shopId, name, sort, visible);
    }
}
