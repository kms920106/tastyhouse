package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopRequestListItemResult(
    Long requestId,
    String requestType,
    String summary,
    String status,
    String rejectReason,
    boolean hasAttachment,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
