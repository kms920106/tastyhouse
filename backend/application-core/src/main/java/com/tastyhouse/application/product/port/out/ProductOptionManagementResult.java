package com.tastyhouse.application.product.port.out;

public record ProductOptionManagementResult(
    Long id,
    String name,
    Integer additionalPrice,
    Integer sort,
    boolean soldOut,
    boolean visible,
    Integer cupCount,
    Integer personalCupDiscountAmount
) {
}
