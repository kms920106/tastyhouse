package com.tastyhouse.infrastructure.notification.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;

final class NotificationMapper {
    private NotificationMapper() {
    }

    static Notification toDomain(NotificationJpaEntity entity) {
        return Notification.reconstitute(
            entity.getId(),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getType() == null ? null : NotificationType.valueOf(entity.getType()),
            entity.getTitle(),
            entity.getBody(),
            entity.getTargetType() == null ? null : NotificationTargetType.valueOf(entity.getTargetType()),
            entity.getTargetId(),
            entity.isRead(),
            entity.getReadAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static NotificationJpaEntity toEntity(Notification notification) {
        return NotificationJpaEntity.create(
            notification.getMemberId() == null ? null : notification.getMemberId().value(),
            notification.getType() == null ? null : notification.getType().name(),
            notification.getTitle(),
            notification.getBody(),
            notification.getTargetType() == null ? null : notification.getTargetType().name(),
            notification.getTargetId(),
            notification.isRead(),
            notification.getReadAt()
        );
    }

    static void applyChanges(NotificationJpaEntity entity, Notification notification) {
        entity.applyChanges(notification.isRead(), notification.getReadAt());
    }
}
