package com.tastyhouse.core.domain.order.application.dto.result;

import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResult(
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
    List<OrderProductResult> orderProducts,
    PaymentResult payment,
    LocalDateTime approvedAt,
    LocalDateTime createdAt
) {
    public record PaymentResult(
        Long id,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        Integer amount,
        String cardCompany,
        String cardNumber,
        LocalDateTime approvedAt,
        String receiptUrl
    ) {
        public static PaymentResult from(Payment payment) {
            return new PaymentResult(
                payment.getId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getAmount() != null ? payment.getAmount().value() : null,
                payment.getCardCompany(),
                payment.getCardNumber(),
                payment.getApprovedAt(),
                payment.getReceiptUrl()
            );
        }
    }

    public static OrderResult from(
        Order order,
        String shopName,
        String shopPhoneNumber,
        List<OrderProductResult> orderProducts,
        Payment payment
    ) {
        PaymentResult paymentResult = payment != null ? PaymentResult.from(payment) : null;
        return new OrderResult(
            order.getId(),
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
