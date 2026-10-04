package com.tastyhouse.application.notification.port.in;

public interface NotificationCommandUseCase {

    void markAsRead(NotificationMarkAsReadCommand command);

    void markAllAsRead(NotificationMarkAllAsReadCommand command);
}
