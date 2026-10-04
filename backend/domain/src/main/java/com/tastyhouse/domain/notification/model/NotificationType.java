package com.tastyhouse.domain.notification.model;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public enum NotificationType {

    REVIEW_OWNER_REPLY,

    REVIEW_BLIND_APPROVED;

    public static NotificationType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new DomainException(DomainErrorCode.NOTIFICATION_TYPE_UNKNOWN,
                DomainErrorCode.NOTIFICATION_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
