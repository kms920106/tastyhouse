package com.tastyhouse.application.product.port.out;

public record BatchOptionResult(
    Long id,
    String name,
    Integer price,
    Integer cupCount,
    Integer depositAmount,
    Integer personalCupDiscountAmount
) {
}
