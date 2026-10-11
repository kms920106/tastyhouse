package com.tastyhouse.application.notification.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.vo.NotificationId;

public interface NotificationLoadPort {

    Optional<Notification> findById(NotificationId notificationId);

    List<Notification> findUnreadByMemberId(MemberId memberId);
}
