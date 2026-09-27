package com.tastyhouse.application.notification.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.notification.port.out.write.NotificationState;
import com.tastyhouse.application.notification.port.out.write.NotificationStatePort;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.vo.NotificationId;

public class NotificationStore implements NotificationRepository {
    private final NotificationStatePort notificationStatePort;

    public NotificationStore(NotificationStatePort notificationStatePort) {
        this.notificationStatePort = notificationStatePort;
    }

    @Override
    public Optional<Notification> findById(NotificationId notificationId) {
        return notificationStatePort.findById(notificationId.value()).map(NotificationStateMapper::toDomain);
    }

    @Override
    public List<Notification> findUnreadByMemberId(MemberId memberId) {
        return notificationStatePort.findUnreadByMemberId(memberId.value()).stream()
            .map(NotificationStateMapper::toDomain)
            .toList();
    }

    @Override
    public Notification save(Notification notification) {
        return NotificationStateMapper.toDomain(notificationStatePort.save(NotificationStateMapper.toState(notification)));
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        List<NotificationState> states = notifications.stream()
            .map(NotificationStateMapper::toState)
            .toList();
        return notificationStatePort.saveAll(states).stream()
            .map(NotificationStateMapper::toDomain)
            .toList();
    }
}
