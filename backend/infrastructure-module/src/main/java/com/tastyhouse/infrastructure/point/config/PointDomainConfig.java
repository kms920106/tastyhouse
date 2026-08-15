package com.tastyhouse.infrastructure.point.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.point.repository.PointHistoryRepository;
import com.tastyhouse.domain.point.repository.PointRepository;
import com.tastyhouse.domain.point.service.PointLedgerService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * point 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class PointDomainConfig {

    /**
     * 포인트 원장 — 잔액 변경과 변동 이력 기록을 한 트랜잭션에서 함께 처리하는 오케스트레이션.
     */
    @Bean
    public PointLedgerService pointLedgerService(
        PointRepository pointRepository,
        PointHistoryRepository pointHistoryRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PointLedgerService(pointRepository, pointHistoryRepository, domainEventPublisher);
    }
}
