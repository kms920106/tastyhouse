package com.tastyhouse.infrastructure.jpa.product.query;

public record ProductBatchOptionRow(
    Long id,
    Long optionGroupId,
    String name,
    Integer additionalPrice,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
}
