package com.tastyhouse.application.product.port.out.write;

public record ProductCategoryState(
    Long id,
    Long shopId,
    String name,
    String description,
    Integer sort,
    boolean visible
) {
}
