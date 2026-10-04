package com.tastyhouse.application.notification.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record NotificationMarkAsReadCommand(
    Long notificationId,
    Long memberId
) {

    public NotificationMarkAsReadCommand {
        if (notificationId == null || memberId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static NotificationMarkAsReadCommand of(Long notificationId, Long memberId) {
        return new NotificationMarkAsReadCommand(notificationId, memberId);
    }
}
