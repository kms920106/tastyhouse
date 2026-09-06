package com.tastyhouse.application.order.port.out;

public record OrderProductOwnershipResult(
    Long orderId,
    Long orderMemberId,
    Long productId,
    String orderMethod
) {
}
