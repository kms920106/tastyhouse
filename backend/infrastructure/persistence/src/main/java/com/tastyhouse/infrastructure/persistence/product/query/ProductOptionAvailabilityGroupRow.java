package com.tastyhouse.infrastructure.persistence.product.query;

public record ProductOptionAvailabilityGroupRow(
    Long id,
    String name,
    Boolean required,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    String productName
) {
}
