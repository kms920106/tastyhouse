package com.tastyhouse.infrastructure.persistence.product.query;

public record ProductCommonOptionRow(
    Long commonOptionGroupId,
    Long id,
    String name,
    Integer additionalPrice,
    Boolean soldOut
) {
}
