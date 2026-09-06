package com.tastyhouse.infrastructure.notification.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class NotificationMapper {
    private NotificationMapper() {
    }

    static Notification toDomain(NotificationJpaEntity entity) {
        return Notification.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getType(),
            entity.getTitle(),
            entity.getBody(),
            entity.getTargetType(),
            entity.getTargetId(),
            entity.isRead(),
            entity.getReadAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static NotificationJpaEntity toEntity(Notification domain) {
        return NotificationJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getType(),
            domain.getTitle(),
            domain.getBody(),
            domain.getTargetType(),
            domain.getTargetId(),
            domain.isRead(),
            domain.getReadAt()
        );
    }

    static void applyChanges(NotificationJpaEntity entity, Notification domain) {
        entity.applyChanges(domain.isRead(), domain.getReadAt());
    }
}
