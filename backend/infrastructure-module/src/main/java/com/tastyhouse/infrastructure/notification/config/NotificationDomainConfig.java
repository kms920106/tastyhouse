package com.tastyhouse.infrastructure.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.notification.repository.NotificationRepository;
import com.tastyhouse.domain.notification.service.NotificationService;

/**
 * notification 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class NotificationDomainConfig {

    /**
     * 인앱 알림 적재·읽음 처리 — 알림 문구 소유(유형별 적재 메서드)와 수신자 소유권 검증을 담당한다.
     */
    @Bean
    public NotificationService notificationService(NotificationRepository notificationRepository) {
        return new NotificationService(notificationRepository);
    }
}
