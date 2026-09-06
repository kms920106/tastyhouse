package com.tastyhouse.application.notification.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record NotificationMarkAllAsReadCommand(Long memberId) {
    public NotificationMarkAllAsReadCommand {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static NotificationMarkAllAsReadCommand of(Long memberId) {
        return new NotificationMarkAllAsReadCommand(memberId);
    }
}
