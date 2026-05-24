package com.tastyhouse.core.domain.product.application.dto.command;

public record SaveProductOptionGroupCommand(
    Long productId,
    String name,
    String description,
    Boolean isRequired,
    Boolean isMultipleSelect,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    Boolean isActive
) {}
