package com.tastyhouse.application.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.notification.port.in.NotificationQueryUseCase;
import com.tastyhouse.application.notification.port.out.NotificationListItemResult;
import com.tastyhouse.application.notification.port.out.NotificationQueryPort;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@WebApp
@Transactional(readOnly = true)
public class NotificationQueryService implements NotificationQueryUseCase {

    private final NotificationQueryPort notificationQueryPort;

    public NotificationQueryService(NotificationQueryPort notificationQueryPort) {
        this.notificationQueryPort = notificationQueryPort;
    }

    @Override
    public PageResult<NotificationListItemResult> findNotifications(Long memberId, int page, int size) {
        return notificationQueryPort.findNotificationsByMemberId(memberId, PageQuery.of(page, size));
    }

    @Override
    public long countUnread(Long memberId) {
        return notificationQueryPort.countUnreadByMemberId(memberId);
    }
}
