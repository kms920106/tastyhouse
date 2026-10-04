package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record EventDeleteCommand(Long eventId) {

    public EventDeleteCommand {
        if (eventId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static EventDeleteCommand of(Long eventId) {
        return new EventDeleteCommand(eventId);
    }
}
