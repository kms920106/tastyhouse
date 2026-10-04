package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record EventWinnerDeleteCommand(Long winnerId) {

    public EventWinnerDeleteCommand {
        if (winnerId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static EventWinnerDeleteCommand of(Long winnerId) {
        return new EventWinnerDeleteCommand(winnerId);
    }
}
