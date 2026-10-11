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

    public CeoLoginHistoryResult withDescriptions(String resultDescription, String failureReasonDescription) {
        return new CeoLoginHistoryResult(
            this.id,
            this.result,
            resultDescription,
            this.failureReason,
            failureReasonDescription,
            this.ipAddress,
            this.userAgent,
            this.loggedInAt
        );
    }
}
