package com.tastyhouse.application.notification.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface NotificationCommandUseCase {

    void markAsRead(NotificationMarkAsReadCommand command);

    void markAllAsRead(NotificationMarkAllAsReadCommand command);
}
