package com.tastyhouse.application.point.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.point.port.out.write.PointHistoryStatePort;
import com.tastyhouse.application.point.port.out.write.PointStatePort;
import com.tastyhouse.application.point.service.PointLedgerService;
import com.tastyhouse.application.point.store.PointHistoryRepository;
import com.tastyhouse.application.point.store.PointHistoryStore;
import com.tastyhouse.application.point.store.PointRepository;
import com.tastyhouse.application.point.store.PointStore;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class PointServiceConfig {
    @Bean
    public PointRepository pointRepository(PointStatePort pointStatePort) {
        return new PointStore(pointStatePort);
    }

    @Bean
    public PointHistoryRepository pointHistoryRepository(PointHistoryStatePort pointHistoryStatePort) {
        return new PointHistoryStore(pointHistoryStatePort);
    }

    @Bean
    public PointLedgerService pointLedgerService(
        PointRepository pointRepository,
        PointHistoryRepository pointHistoryRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PointLedgerService(pointRepository, pointHistoryRepository, domainEventPublisher);
    }
}
