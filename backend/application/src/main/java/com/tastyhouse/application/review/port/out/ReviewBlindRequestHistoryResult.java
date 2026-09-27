package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

public record ReviewBlindRequestHistoryResult(
    Long id,
    String reason,
    String reasonDescription,
    String detailReason,
    String status,
    String statusDescription,
    String rejectReason,
    LocalDateTime blindUntil,
    LocalDateTime createdAt
) {
}
