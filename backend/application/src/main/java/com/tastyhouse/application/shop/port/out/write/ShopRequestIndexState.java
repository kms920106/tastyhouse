package com.tastyhouse.application.shop.port.out.write;

import java.time.LocalDateTime;

public record ShopRequestIndexState(
    Long id,
    Long shopId,
    String requestType,
    Long sourceRequestId,
    String summary,
    String status,
    String rejectReason,
    Long attachmentFileId,
    Long requestedByCeoId,
    LocalDateTime processedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
