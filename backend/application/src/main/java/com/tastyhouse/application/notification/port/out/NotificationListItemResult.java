package com.tastyhouse.application.notification.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;

public record NotificationListItemResult(
    Long id,
    NotificationType type,
    String title,
    String body,
    NotificationTargetType targetType,
    Long targetId,
    boolean read,
    LocalDateTime createdAt
) {
}
