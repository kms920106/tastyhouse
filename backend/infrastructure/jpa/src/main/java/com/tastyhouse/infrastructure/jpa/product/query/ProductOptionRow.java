package com.tastyhouse.infrastructure.jpa.product.query;

public record ProductOptionRow(
    Long optionGroupId,
    Long id,
    String name,
    Integer additionalPrice,
    Boolean soldOut,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
}
