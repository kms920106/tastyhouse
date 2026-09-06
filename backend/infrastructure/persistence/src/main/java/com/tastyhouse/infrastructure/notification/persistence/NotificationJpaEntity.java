package com.tastyhouse.infrastructure.notification.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "NOTIFICATION")
public class NotificationJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30, columnDefinition = "VARCHAR(30)")
    private NotificationTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    protected NotificationJpaEntity() {
    }

    private NotificationJpaEntity(
        Long memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId,
        boolean read,
        LocalDateTime readAt
    ) {
        this.memberId = memberId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.targetType = targetType;
        this.targetId = targetId;
        this.read = read;
        this.readAt = readAt;
    }

    static NotificationJpaEntity create(
        Long memberId,
        NotificationType type,
        String title,
        String body,
        NotificationTargetType targetType,
        Long targetId,
        boolean read,
        LocalDateTime readAt
    ) {
        return new NotificationJpaEntity(memberId, type, title, body, targetType, targetId, read, readAt);
    }

    void applyChanges(boolean read, LocalDateTime readAt) {
        this.read = read;
        this.readAt = readAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
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
}
