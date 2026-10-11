package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

public record OrderManagementListItemResult(
    Long id,
    String orderNumber,
    String shopName,
    String ordererName,
    String orderMethod,
    String orderStatus,
    String paymentStatus,
    Integer finalAmount,
    Integer totalItemCount,
    LocalDateTime createdAt,

    LocalDateTime scheduledAt
) {
}
