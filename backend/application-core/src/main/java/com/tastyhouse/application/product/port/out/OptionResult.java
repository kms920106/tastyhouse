package com.tastyhouse.application.product.port.out;

public record OptionResult(
    Long id,
    String name,
    Integer additionalPrice,
    boolean soldOut,
    Integer cupCount,
    Integer depositAmount,
    Integer personalCupDiscountAmount
) {
}
