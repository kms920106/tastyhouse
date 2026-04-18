package com.tastyhouse.webapi.order.response;

import com.tastyhouse.core.entity.order.OrderStatus;

import java.time.LocalDateTime;

public record OrderListItem(
    Long id,
    String orderNumber,
    OrderStatus orderStatus,
    String placeName,
    String firstProductName,
    String firstProductImageUrl,
    Integer totalProductCount,
    Integer finalAmount,
    LocalDateTime createdAt
) {
}
