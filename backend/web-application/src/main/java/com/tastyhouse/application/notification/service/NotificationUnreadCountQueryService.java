package com.tastyhouse.application.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.notification.port.in.NotificationUnreadCountQueryUseCase;
import com.tastyhouse.application.notification.port.out.NotificationQueryPort;

@Service
@Transactional(readOnly = true)
class NotificationUnreadCountQueryService implements NotificationUnreadCountQueryUseCase {

    private final NotificationQueryPort notificationQueryPort;

    public NotificationUnreadCountQueryService(NotificationQueryPort notificationQueryPort) {
        this.notificationQueryPort = notificationQueryPort;
    }

    @Override
    public long countUnread(Long memberId) {
        return notificationQueryPort.countUnreadByMemberId(memberId);
    }
}
