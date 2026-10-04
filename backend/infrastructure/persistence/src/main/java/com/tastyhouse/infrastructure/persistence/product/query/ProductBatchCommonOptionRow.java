package com.tastyhouse.infrastructure.persistence.product.query;

public record ProductBatchCommonOptionRow(
    Long id,
    Long commonOptionGroupId,
    String name,
    Integer additionalPrice
) {
}
