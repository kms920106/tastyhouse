package com.tastyhouse.application.product.port.out;

public record ProductCategoryManagementResult(
    Long id,
    Long shopId,
    String name,
    String description,
    Integer sort,
    boolean visible,
    long productCount
) {
}
