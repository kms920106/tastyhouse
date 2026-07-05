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
) {

    public static SaveProductOptionGroupCommand of(
        Long productId,
        String name,
        String description,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        boolean visible
    ) {
        return new SaveProductOptionGroupCommand(
            productId, name, description, required, multipleSelect, minSelect, maxSelect, sort, visible
        );
    }
}
