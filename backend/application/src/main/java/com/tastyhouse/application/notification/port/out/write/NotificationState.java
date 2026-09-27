package com.tastyhouse.application.notification.port.out.write;

import java.time.LocalDateTime;

public record NotificationState(
    Long id,
    Long memberId,
    String type,
    String title,
    String body,
    String targetType,
    Long targetId,
    boolean read,
    LocalDateTime readAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
