package com.tastyhouse.infrastructure.point.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.point.repository.PointHistoryRepository;
import com.tastyhouse.domain.point.repository.PointRepository;
import com.tastyhouse.domain.point.service.PointLedgerService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

@Configuration(proxyBeanMethods = false)
public class PointDomainConfig {
    @Bean
    public PointLedgerService pointLedgerService(
        PointRepository pointRepository,
        PointHistoryRepository pointHistoryRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PointLedgerService(pointRepository, pointHistoryRepository, domainEventPublisher);
    }
}
