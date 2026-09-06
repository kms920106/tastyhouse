package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;

public record OrderDetailResult(
    Long id,
    Long memberId,
    String orderNumber,
    OrderMethod orderMethod,
    OrderStatus orderStatus,
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
    LocalDateTime createdAt,

    LocalDateTime scheduledAt,

    LocalDateTime scheduledSlotEndAt,

    List<OrderProductResult> orderProducts,
    OrderPaymentResult payment
) {

    public OrderDetailResult(
        Long id,
        Long memberId,
        String orderNumber,
        OrderMethod orderMethod,
        OrderStatus orderStatus,
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
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime scheduledSlotEndAt
    ) {
        this(
            id,
            memberId,
            orderNumber,
            orderMethod,
            orderStatus,
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
            cupDepositAmount,
            finalAmount,
            usedPoint,
            earnedPoint,
            createdAt,
            scheduledAt,
            scheduledSlotEndAt,
            List.of(),
            null
        );
    }

    public OrderDetailResult withOrderProducts(List<OrderProductResult> orderProducts) {
        return new OrderDetailResult(
            id,
            memberId,
            orderNumber,
            orderMethod,
            orderStatus,
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
            cupDepositAmount,
            finalAmount,
            usedPoint,
            earnedPoint,
            createdAt,
            scheduledAt,
            scheduledSlotEndAt,
            orderProducts,
            payment
        );
    }

    public OrderDetailResult withPayment(OrderPaymentResult payment) {
        return new OrderDetailResult(
            id,
            memberId,
            orderNumber,
            orderMethod,
            orderStatus,
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
            cupDepositAmount,
            finalAmount,
            usedPoint,
            earnedPoint,
            createdAt,
            scheduledAt,
            scheduledSlotEndAt,
            orderProducts,
            payment
        );
    }
}
