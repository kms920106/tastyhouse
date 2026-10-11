package com.tastyhouse.application.notification.port.out;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface NotificationQueryPort {

    PageResult<NotificationListItemResult> findNotificationsByMemberId(Long memberId, PageQuery pageQuery);

    long countUnreadByMemberId(Long memberId);
}
