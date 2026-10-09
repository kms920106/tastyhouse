package com.tastyhouse.infrastructure.jpa.product.query;

public record ProductBatchCommonOptionRow(
    Long id,
    Long commonOptionGroupId,
    String name,
    Integer additionalPrice
) {
}
