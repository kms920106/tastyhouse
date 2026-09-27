package com.tastyhouse.application.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.notification.port.out.write.NotificationStatePort;
import com.tastyhouse.application.notification.service.NotificationService;
import com.tastyhouse.application.notification.store.NotificationRepository;
import com.tastyhouse.application.notification.store.NotificationStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class NotificationServiceConfig {
    @Bean
    public NotificationRepository notificationRepository(NotificationStatePort notificationStatePort) {
        return new NotificationStore(notificationStatePort);
    }

    @Bean
    public NotificationService notificationService(NotificationRepository notificationRepository) {
        return new NotificationService(notificationRepository);
    }
}
