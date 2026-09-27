package com.tastyhouse.application.point.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.point.port.out.write.PointHistoryRepository;
import com.tastyhouse.application.point.port.out.write.PointRepository;
import com.tastyhouse.application.point.service.PointLedgerService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class PointServiceConfig {
    @Bean
    public PointLedgerService pointLedgerService(
        PointRepository pointRepository,
        PointHistoryRepository pointHistoryRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PointLedgerService(pointRepository, pointHistoryRepository, domainEventPublisher);
    }
}
