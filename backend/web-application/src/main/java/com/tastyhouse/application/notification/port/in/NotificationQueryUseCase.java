package com.tastyhouse.application.notification.port.in;

import com.tastyhouse.application.notification.port.out.NotificationListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface NotificationQueryUseCase {

    PageResult<NotificationListItemResult> findNotifications(Long memberId, int page, int size);

    long countUnread(Long memberId);
}
