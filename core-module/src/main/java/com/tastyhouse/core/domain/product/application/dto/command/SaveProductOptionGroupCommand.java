package com.tastyhouse.core.domain.product.application.dto.command;

public record SaveProductOptionGroupCommand(
    Long productId,
    String name,
    String description,
    boolean required,
    boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    boolean visible
) {}
