package com.tastyhouse.infrastructure.persistence.product.query;

public record ProductCommonOptionGroupRow(
    Long id,
    String name,
    String description,
    Boolean required,
    Boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect
) {
}
