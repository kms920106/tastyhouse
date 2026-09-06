package com.tastyhouse.application.notification.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.notification.port.out.NotificationListItemResult;

@WebApp
public interface NotificationQueryUseCase {

    PageResult<NotificationListItemResult> findNotifications(Long memberId, int page, int size);

    long countUnread(Long memberId);
}
