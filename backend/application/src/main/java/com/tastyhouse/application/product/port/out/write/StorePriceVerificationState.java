package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record StorePriceVerificationState(
    Long id,
    Long shopId,
    Long priceListFileId,
    String status,
    String rejectReason,
    Long requestedByCeoId,
    LocalDateTime processedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
