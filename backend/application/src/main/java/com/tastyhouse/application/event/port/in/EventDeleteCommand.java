package com.tastyhouse.application.event.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record EventDeleteCommand(Long eventId) {
    public EventDeleteCommand {
        if (eventId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static EventDeleteCommand of(Long eventId) {
        return new EventDeleteCommand(eventId);
    }
}
