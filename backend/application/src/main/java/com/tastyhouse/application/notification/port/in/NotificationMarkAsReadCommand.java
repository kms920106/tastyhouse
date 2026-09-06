package com.tastyhouse.application.notification.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record NotificationMarkAsReadCommand(
    Long notificationId,
    Long memberId
) {
    public NotificationMarkAsReadCommand {
        if (notificationId == null || memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static NotificationMarkAsReadCommand of(Long notificationId, Long memberId) {
        return new NotificationMarkAsReadCommand(notificationId, memberId);
    }
}
