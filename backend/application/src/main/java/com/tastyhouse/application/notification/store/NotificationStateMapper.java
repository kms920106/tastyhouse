package com.tastyhouse.application.notification.store;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;
import com.tastyhouse.application.notification.port.out.write.NotificationState;

final class NotificationStateMapper {
    private NotificationStateMapper() {
    }

    static Notification toDomain(NotificationState state) {
        return Notification.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.type() == null ? null : NotificationType.valueOf(state.type()),
            state.title(),
            state.body(),
            state.targetType() == null ? null : NotificationTargetType.valueOf(state.targetType()),
            state.targetId(),
            state.read(),
            state.readAt(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static NotificationState toState(Notification notification) {
        return new NotificationState(
            notification.getId(),
            notification.getMemberId() == null ? null : notification.getMemberId().value(),
            notification.getType() == null ? null : notification.getType().name(),
            notification.getTitle(),
            notification.getBody(),
            notification.getTargetType() == null ? null : notification.getTargetType().name(),
            notification.getTargetId(),
            notification.isRead(),
            notification.getReadAt(),
            notification.getCreatedAt(),
            notification.getUpdatedAt()
        );
    }
}
