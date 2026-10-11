package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailViewResult(
    Long id,
    String orderNumber,
    String orderMethod,
    String paymentStatus,
    String shopName,
    String shopPhoneNumber,
    String ordererName,
    String ordererPhone,
    String ordererEmail,
    Integer totalProductAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer pointDiscountAmount,
    Integer totalDiscountAmount,
    Integer cupDepositAmount,
    Integer finalAmount,
    Integer usedPoint,
    Integer earnedPoint,
    List<OrderProductViewResult> orderProducts,
    OrderPaymentSummaryResult payment,
    LocalDateTime paymentApprovedAt,
    LocalDateTime createdAt,
    LocalDateTime scheduledAt,
    LocalDateTime scheduledSlotEndAt
) {
}
