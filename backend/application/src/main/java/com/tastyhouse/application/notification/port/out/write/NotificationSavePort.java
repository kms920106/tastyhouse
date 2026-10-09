package com.tastyhouse.application.notification.port.out.write;

import java.util.List;

import com.tastyhouse.domain.notification.model.Notification;

public interface NotificationSavePort {

    Notification save(Notification notification);

    List<Notification> saveAll(List<Notification> notifications);
}
