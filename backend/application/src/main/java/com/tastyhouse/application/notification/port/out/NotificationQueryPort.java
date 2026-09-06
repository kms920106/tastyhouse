package com.tastyhouse.application.notification.port.out;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface NotificationQueryPort {

    PageResult<NotificationListItemResult> findNotificationsByMemberId(Long memberId, PageQuery pageQuery);

    long countUnreadByMemberId(Long memberId);
}
