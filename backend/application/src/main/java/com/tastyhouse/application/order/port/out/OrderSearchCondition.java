package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;

public record OrderSearchCondition(
    Long shopId,
    OrderStatus orderStatus,
    OrderMethod orderMethod,
    PaymentStatus paymentStatus,
    String orderNumber,
    String ordererName,
    LocalDateTime startDate,
    LocalDateTime endDate
) {

    public static OrderSearchCondition of(
        Long shopId,
        OrderStatus orderStatus,
        OrderMethod orderMethod,
        PaymentStatus paymentStatus,
        String orderNumber,
        String ordererName,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return new OrderSearchCondition(
            shopId,
            orderStatus,
            orderMethod,
            paymentStatus,
            orderNumber,
            ordererName,
            startDate,
            endDate
        );
    }
}
