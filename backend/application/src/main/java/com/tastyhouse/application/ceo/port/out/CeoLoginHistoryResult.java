package com.tastyhouse.application.ceo.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;

public record CeoLoginHistoryResult(
    Long id,
    CeoLoginResult result,
    CeoLoginFailureReason failureReason,
    String ipAddress,
    String userAgent,
    LocalDateTime loggedInAt
) {
}
