package com.tastyhouse.infrastructure.persistence.product.query;

public record ProductLinkedProductRow(
    Long optionGroupId,
    Long id,
    Long shopId,
    String name
) {
}
