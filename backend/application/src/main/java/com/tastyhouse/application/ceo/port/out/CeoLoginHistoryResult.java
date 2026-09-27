package com.tastyhouse.application.ceo.port.out;

import java.time.LocalDateTime;

public record CeoLoginHistoryResult(
    Long id,
    String result,
    String resultDescription,
    String failureReason,
    String failureReasonDescription,
    String ipAddress,
    String userAgent,
    LocalDateTime loggedInAt
) {
}
