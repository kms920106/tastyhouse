package com.tastyhouse.infrastructure.persistence.product.query;

public record ProductOptionManagementRow(
    Long optionGroupId,
    Long id,
    String name,
    Integer additionalPrice,
    Integer sort,
    Boolean soldOut,
    Boolean visible,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
}
