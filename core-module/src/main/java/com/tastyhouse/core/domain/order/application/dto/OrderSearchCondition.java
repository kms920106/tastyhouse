package com.tastyhouse.core.domain.order.application.dto;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.order.domain.model.OrderStatus;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

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
        return new OrderSearchCondition(shopId, orderStatus, orderMethod, paymentStatus, orderNumber, ordererName, startDate, endDate);
    }
}
