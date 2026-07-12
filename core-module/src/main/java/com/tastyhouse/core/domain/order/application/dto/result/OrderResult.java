package com.tastyhouse.core.domain.order.application.dto.result;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

public record OrderResult(
    OrderId orderId,
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
    List<OrderProductResult> orderProducts,
    OrderPaymentResult payment,
    LocalDateTime approvedAt,
    LocalDateTime createdAt
) {
    public static OrderResult from(
        Order order,
        String shopName,
        String shopPhoneNumber,
        List<OrderProductResult> orderProducts,
        Payment payment
    ) {
        OrderPaymentResult paymentResult = payment != null ? OrderPaymentResult.from(payment) : null;
        return new OrderResult(
            order.getOrderId(),
            order.getOrderNumber(),
            order.getOrderMethod(),
            payment != null ? payment.getPaymentStatus() : null,
            shopName,
            shopPhoneNumber,
            order.getOrdererName(),
            order.getOrdererPhone(),
            order.getOrdererEmail(),
            order.getTotalProductAmount(),
            order.getProductDiscountAmount(),
            order.getCouponDiscountAmount(),
            order.getPointDiscountAmount(),
            order.getTotalDiscountAmount(),
            order.getFinalAmount(),
            order.getUsedPoint(),
            order.getEarnedPoint(),
            orderProducts,
            paymentResult,
            payment != null ? payment.getApprovedAt() : null,
            order.getCreatedAt()
        );
    }
}
