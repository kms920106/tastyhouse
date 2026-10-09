package com.tastyhouse.infrastructure.jpa.product.query;

public record ProductOptionGroupRow(
    Long id,
    String name,
    String description,
    Boolean required,
    Boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    String groupType
) {
}
