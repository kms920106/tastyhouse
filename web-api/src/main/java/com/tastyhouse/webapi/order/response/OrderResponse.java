package com.tastyhouse.webapi.order.response;

import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderNumber,
    PaymentStatus paymentStatus,
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
    Integer finalAmount,
    Integer usedPoint,
    Integer earnedPoint,
    List<OrderItemResponse> orderItems,
    PaymentSummaryResponse payment,
    LocalDateTime approvedAt,
    LocalDateTime createdAt
) {
    public static OrderResponse from(
    Long id,
    String orderNumber,
    PaymentStatus paymentStatus,
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
    Integer finalAmount,
    Integer usedPoint,
    Integer earnedPoint,
    List<OrderItemResponse> orderItems,
    PaymentSummaryResponse payment,
    LocalDateTime approvedAt,
    LocalDateTime createdAt
    ) {
    return new OrderResponse(
        id,
        orderNumber,
        paymentStatus,
        shopName,
        shopPhoneNumber,
        ordererName,
        ordererPhone,
        ordererEmail,
        totalProductAmount,
        productDiscountAmount,
        couponDiscountAmount,
        pointDiscountAmount,
        totalDiscountAmount,
        finalAmount,
        usedPoint,
        earnedPoint,
        orderItems,
        payment,
        approvedAt,
        createdAt
    );
    }
}
