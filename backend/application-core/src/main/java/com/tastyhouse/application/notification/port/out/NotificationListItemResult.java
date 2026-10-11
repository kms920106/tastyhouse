package com.tastyhouse.application.notification.port.out;

import java.time.LocalDateTime;

public record NotificationListItemResult(
    Long id,
    String type,
    String title,
    String body,
    String targetType,
    Long targetId,
    boolean read,
    LocalDateTime createdAt
) {
}
