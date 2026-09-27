package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

public record ShopRequestListItemViewResult(
    Long requestId,
    String requestType,
    String requestTypeDescription,
    String summary,
    String status,
    String statusDescription,
    String rejectReason,
    boolean contractAmending,
    boolean hasAttachment,
    long commentCount,
    LocalDateTime requestedAt,
    LocalDateTime processedAt
) {
}
