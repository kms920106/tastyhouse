package com.tastyhouse.infrastructure.jpa.product.query;

public record ProductCommonOptionRow(
    Long commonOptionGroupId,
    Long id,
    String name,
    Integer additionalPrice,
    Boolean soldOut
) {
}
