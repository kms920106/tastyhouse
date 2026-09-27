package com.tastyhouse.application.review.port.out.write;

import java.time.LocalDateTime;

public record ReviewBlindRequestState(
    Long id,
    Long reviewId,
    Long shopId,
    Long ceoId,
    String reason,
    String detailReason,
    String status,
    String rejectReason,
    LocalDateTime blindUntil,
    LocalDateTime createdAt
) {
}
