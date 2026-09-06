package com.tastyhouse.infrastructure.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.notification.repository.NotificationRepository;
import com.tastyhouse.domain.notification.service.NotificationService;

@Configuration(proxyBeanMethods = false)
public class NotificationDomainConfig {
    @Bean
    public NotificationService notificationService(NotificationRepository notificationRepository) {
        return new NotificationService(notificationRepository);
    }
}
