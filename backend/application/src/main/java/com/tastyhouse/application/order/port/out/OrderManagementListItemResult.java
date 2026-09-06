package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;

public record OrderManagementListItemResult(
    Long id,
    String orderNumber,
    String shopName,
    String ordererName,
    OrderMethod orderMethod,
    OrderStatus orderStatus,
    PaymentStatus paymentStatus,
    Integer finalAmount,
    Integer totalItemCount,
    LocalDateTime createdAt,

    LocalDateTime scheduledAt
) {
}
