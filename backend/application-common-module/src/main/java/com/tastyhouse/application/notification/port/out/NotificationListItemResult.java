package com.tastyhouse.application.notification.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;

/**
 * 알림함 목록 항목 조회 결과.
 *
 * <p>{@code targetType}/{@code targetId}는 이동 대상이 없는 알림이면 함께 null이다.
 */
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
