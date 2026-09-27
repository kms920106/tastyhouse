package com.tastyhouse.application.product.port.out.write;

public record ProductCommonOptionGroupState(
    Long id,
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
}
