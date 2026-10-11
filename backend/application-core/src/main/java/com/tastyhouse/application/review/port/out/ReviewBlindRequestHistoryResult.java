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

    public ReviewBlindRequestHistoryResult withDescriptions(String reasonDescription, String statusDescription) {
        return new ReviewBlindRequestHistoryResult(
            this.id,
            this.reason,
            reasonDescription,
            this.detailReason,
            this.status,
            statusDescription,
            this.rejectReason,
            this.blindUntil,
            this.createdAt
        );
    }
}
