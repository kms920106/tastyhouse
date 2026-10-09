package com.tastyhouse.infrastructure.jpa.product.query;

public record ProductOptionGroupManagementRow(
    Long id,
    String name,
    String description,
    Boolean required,
    Boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    Boolean visible,
    String groupType,
    Long linkedProductCount
) {
}
