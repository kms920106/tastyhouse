package com.tastyhouse.application.ceo.port.out.write;

import java.time.LocalDateTime;

public record CeoLoginHistoryState(
    Long id,
    Long ceoId,
    String result,
    String failureReason,
    String ipAddress,
    String userAgent,
    LocalDateTime createdAt
) {
}
