package com.tastyhouse.application.product.port.out;

public record ProductCategoryResult(
    Long id,
    Long shopId,
    String name,
    String description,
    Integer sort,
    boolean visible
) {
}
