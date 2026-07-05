package com.tastyhouse.core.domain.product.application.dto.command;

public record CreateProductCategoryCommand(
    Long shopId,
    String name,
    Integer sort,
    boolean visible
) {

    public static CreateProductCategoryCommand of(Long shopId, String name, Integer sort, boolean visible) {
        return new CreateProductCategoryCommand(shopId, name, sort, visible);
    }
}
