package com.tastyhouse.application.notification.port.in;

public interface NotificationMarkAsReadUseCase {

    void markAsRead(NotificationMarkAsReadCommand command);
}
