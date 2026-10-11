package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopRequestDetailResult(
    Long requestId,
    Long shopId,
    String requestType,
    Long sourceRequestId,
    String summary,
    String status,
    String rejectReason,
    String attachmentUrl,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
