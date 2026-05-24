package com.tastyhouse.core.domain.product.application.dto.command;

public record CreateProductCategoryCommand(
    Long placeId,
    String name,
    Integer sort,
    Boolean isActive
) {}
