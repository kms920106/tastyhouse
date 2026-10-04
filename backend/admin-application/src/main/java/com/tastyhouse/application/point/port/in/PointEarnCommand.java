package com.tastyhouse.application.point.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PointEarnCommand(
    Long memberId,
    Integer amount,
    String reason
) {

    public PointEarnCommand {
        if (memberId == null || amount == null || reason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
