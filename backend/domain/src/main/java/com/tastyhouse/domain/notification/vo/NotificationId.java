package com.tastyhouse.domain.notification.vo;

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
