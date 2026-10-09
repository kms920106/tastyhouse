package com.tastyhouse.infrastructure.jpa.product.query;

public record ProductLinkedProductRow(
    Long optionGroupId,
    Long id,
    Long shopId,
    String name
) {
}
