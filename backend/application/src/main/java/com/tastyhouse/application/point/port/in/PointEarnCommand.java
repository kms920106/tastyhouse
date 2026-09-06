package com.tastyhouse.application.point.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PointEarnCommand(
    Long memberId,
    Integer amount,
    String reason
) {
    public PointEarnCommand {
        if (memberId == null || amount == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
