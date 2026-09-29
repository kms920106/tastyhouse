package com.tastyhouse.application.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.notification.port.out.write.NotificationPersistencePort;
import com.tastyhouse.application.notification.service.NotificationService;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class NotificationServiceConfig {
    @Bean
    public NotificationService notificationService(NotificationPersistencePort notificationPersistencePort) {
        return new NotificationService(notificationPersistencePort);
    }
}
