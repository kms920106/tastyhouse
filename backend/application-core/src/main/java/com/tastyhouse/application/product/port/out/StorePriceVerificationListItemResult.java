package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

public record StorePriceVerificationListItemResult(
    Long id,
    Long shopId,
    String shopName,
    String status,
    String priceListFileUrl,
    String rejectReason,
    Long itemCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
