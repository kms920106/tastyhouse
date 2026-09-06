package com.tastyhouse.domain.notification.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum NotificationTargetType {
    REVIEW;

    public static NotificationTargetType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.NOTIFICATION_TYPE_UNKNOWN,
                ErrorCode.NOTIFICATION_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
