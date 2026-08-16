package com.tastyhouse.domain.notification.vo;

/**
 * 알림 식별자.
 */
public record NotificationId(Long value) {

    public NotificationId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("NotificationId는 양수여야 합니다: " + value);
        }
    }

    public static NotificationId of(Long value) {
        return new NotificationId(value);
    }
}
