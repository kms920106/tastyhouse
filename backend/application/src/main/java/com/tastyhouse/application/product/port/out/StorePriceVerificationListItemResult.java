package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;

public record StorePriceVerificationListItemResult(
    Long id,
    Long shopId,
    String shopName,
    StorePriceVerificationStatus status,
    String priceListFileUrl,
    String rejectReason,
    Long itemCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
