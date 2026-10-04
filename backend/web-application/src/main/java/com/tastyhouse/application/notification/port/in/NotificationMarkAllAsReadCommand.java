package com.tastyhouse.application.notification.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record NotificationMarkAllAsReadCommand(Long memberId) {

    public NotificationMarkAllAsReadCommand {
        if (memberId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static NotificationMarkAllAsReadCommand of(Long memberId) {
        return new NotificationMarkAllAsReadCommand(memberId);
    }
}
