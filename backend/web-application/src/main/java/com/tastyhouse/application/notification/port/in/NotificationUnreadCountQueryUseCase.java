package com.tastyhouse.application.notification.port.in;

public interface NotificationUnreadCountQueryUseCase {

    long countUnread(Long memberId);
}
