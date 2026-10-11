package com.tastyhouse.application.product.port.out;

public record ProductImageManagementResult(
    Long id,
    String imageUrl,
    Integer sort,
    boolean visible
) {
}
