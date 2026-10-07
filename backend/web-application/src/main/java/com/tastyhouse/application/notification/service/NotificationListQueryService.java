package com.tastyhouse.application.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.notification.port.in.NotificationListQueryUseCase;
import com.tastyhouse.application.notification.port.out.NotificationListItemResult;
import com.tastyhouse.application.notification.port.out.NotificationQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class NotificationListQueryService implements NotificationListQueryUseCase {

    private final NotificationQueryPort notificationQueryPort;

    public NotificationListQueryService(NotificationQueryPort notificationQueryPort) {
        this.notificationQueryPort = notificationQueryPort;
    }

    @Override
    public PageResult<NotificationListItemResult> findNotifications(Long memberId, int page, int size) {
        return notificationQueryPort.findNotificationsByMemberId(memberId, PageQuery.of(page, size));
    }
}
