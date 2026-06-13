package com.tastyhouse.webapi.order.response;

import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
    Long id,
    String orderNumber,
    OrderMethod orderMethod,
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
    List<OrderProductResponse> orderProducts,
    PaymentSummaryResponse payment,
    LocalDateTime approvedAt,
    LocalDateTime createdAt
) {
    public static OrderDetailResponse from(
        Long id,
        String orderNumber,
        OrderMethod orderMethod,
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
        List<OrderProductResponse> orderProducts,
        PaymentSummaryResponse payment,
        LocalDateTime approvedAt,
        LocalDateTime createdAt
    ) {
        return new OrderDetailResponse(
            id,
            orderNumber,
            orderMethod,
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
            orderProducts,
            payment,
            approvedAt,
            createdAt
        );
    }
}
