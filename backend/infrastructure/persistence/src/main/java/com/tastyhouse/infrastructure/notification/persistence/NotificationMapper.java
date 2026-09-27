package com.tastyhouse.infrastructure.notification.persistence;

import com.tastyhouse.application.notification.port.out.write.NotificationState;

final class NotificationMapper {
    private NotificationMapper() {
    }

    static NotificationState toState(NotificationJpaEntity entity) {
        return new NotificationState(
            entity.getId(),
            entity.getMemberId(),
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

    static NotificationJpaEntity toEntity(NotificationState state) {
        return NotificationJpaEntity.create(
            state.memberId(),
            state.type(),
            state.title(),
            state.body(),
            state.targetType(),
            state.targetId(),
            state.read(),
            state.readAt()
        );
    }

    static void applyChanges(NotificationJpaEntity entity, NotificationState state) {
        entity.applyChanges(state.read(), state.readAt());
    }
}
