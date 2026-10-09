package com.tastyhouse.infrastructure.jpa.product.query;

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
