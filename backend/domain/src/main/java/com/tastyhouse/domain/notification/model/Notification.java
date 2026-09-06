package com.tastyhouse.domain.notification.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;

public class Notification {
    private final Long id;
    private final MemberId memberId;
    private final NotificationType type;
    private final String title;
    private final String body;
    private final NotificationTargetType targetType;
    private final Long targetId;
    private boolean read;
    private LocalDateTime readAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Notification(
        Long id,
        MemberId memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.targetType = targetType;
        this.targetId = targetId;
        this.read = read;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Notification of(
        MemberId memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId
    ) {
        return new Notification(null, memberId, type, title, body, targetType, targetId, false, null, null, null);
    }

    public static Notification reconstitute(
        Long id,
        MemberId memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Notification(id, memberId, type, title, body, targetType, targetId, read, readAt, createdAt, updatedAt);
    }

    public void markAsRead(LocalDateTime readAt) {
        if (this.read) {
            return;
        }
        this.read = true;
        this.readAt = readAt;
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public NotificationType getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public String getBody() {
        return this.body;
    }

    public NotificationTargetType getTargetType() {
        return this.targetType;
    }

    public Long getTargetId() {
        return this.targetId;
    }

    public boolean isRead() {
        return this.read;
    }

    public LocalDateTime getReadAt() {
        return this.readAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
