package com.tastyhouse.infrastructure.policy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.policy.repository.PolicyDocumentRepository;
import com.tastyhouse.domain.policy.service.PolicyActivationService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * policy 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class PolicyDomainConfig {

    /**
     * 정책 활성화 규칙 — 같은 유형의 기존 현행 정책을 함께 비활성화하는 크로스 인스턴스 불변식.
     */
    @Bean
    public PolicyActivationService policyActivationService(
        PolicyDocumentRepository policyDocumentRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new PolicyActivationService(policyDocumentRepository, domainEventPublisher);
    }
}
