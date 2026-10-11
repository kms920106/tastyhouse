package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

public record OrderListItemResult(
    Long id,
    String shopName,
    String shopThumbnailImageUrl,
    String firstProductName,
    Integer totalItemCount,
    Integer amount,
    String paymentStatus,
    LocalDateTime paymentDate,

    LocalDateTime scheduledAt
) {
}
