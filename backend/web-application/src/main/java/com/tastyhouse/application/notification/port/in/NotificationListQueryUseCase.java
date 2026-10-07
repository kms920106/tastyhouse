package com.tastyhouse.application.notification.port.in;

import com.tastyhouse.application.notification.port.out.NotificationListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface NotificationListQueryUseCase {

    PageResult<NotificationListItemResult> findNotifications(Long memberId, int page, int size);
}
