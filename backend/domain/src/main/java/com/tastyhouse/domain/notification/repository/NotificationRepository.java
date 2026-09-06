package com.tastyhouse.domain.notification.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.vo.NotificationId;

public interface NotificationRepository {
    Optional<Notification> findById(NotificationId notificationId);

    List<Notification> findUnreadByMemberId(MemberId memberId);

    Notification save(Notification notification);

    List<Notification> saveAll(List<Notification> notifications);
}
