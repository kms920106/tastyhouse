package com.tastyhouse.application.event.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record EventWinnerDeleteCommand(Long winnerId) {
    public EventWinnerDeleteCommand {
        if (winnerId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static EventWinnerDeleteCommand of(Long winnerId) {
        return new EventWinnerDeleteCommand(winnerId);
    }
}
