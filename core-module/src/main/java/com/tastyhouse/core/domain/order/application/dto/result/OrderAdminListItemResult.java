package com.tastyhouse.core.domain.order.application.dto.result;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.order.domain.model.OrderStatus;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;

public record OrderAdminListItemResult(
    Long id,
    String orderNumber,
    String shopName,
    String ordererName,
    OrderMethod orderMethod,
    OrderStatus orderStatus,
    PaymentStatus paymentStatus,
    Integer finalAmount,
    Integer totalItemCount,
    LocalDateTime createdAt
) {
    @QueryProjection
    public OrderAdminListItemResult {
    }
}
