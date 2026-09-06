package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.model.PaymentStatus;

public record OrderListItemResult(
    Long id,
    String shopName,
    String shopThumbnailImageUrl,
    String firstProductName,
    Integer totalItemCount,
    Integer amount,
    PaymentStatus paymentStatus,
    LocalDateTime paymentDate,

    LocalDateTime scheduledAt
) {
}
