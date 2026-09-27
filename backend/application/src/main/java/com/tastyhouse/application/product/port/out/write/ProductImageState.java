package com.tastyhouse.application.product.port.out.write;

public record ProductImageState(
    Long id,
    Long productId,
    Long imageFileId,
    Integer sort,
    boolean visible
) {
}
