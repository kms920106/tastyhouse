package com.tastyhouse.application.order.port.out.write;

public record OrderProductState(
    Long id,
    Long orderId,
    Long productId,
    String name,
    String priceName,
    Long imageFileId,
    Integer quantity,
    Integer originalPrice,
    Integer discountPrice,
    Integer totalOptionPrice,
    Integer totalPrice,
    Integer cupDepositAmount
) {
}
