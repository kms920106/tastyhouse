package com.tastyhouse.infrastructure.menureview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.menureview.repository.MenuReviewRepository;
import com.tastyhouse.domain.menureview.service.MenuReviewLifecycleService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * menureview 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>domain-module에는 {@code @Service}가 0건이라 Spring이 스캔할 수 없으므로, 이 컨텍스트에 새 POJO
 * 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class MenuReviewDomainConfig {

    /**
     * 메뉴 평가 생애주기 — 주문 항목당 1건 불변식과 본인 소유권을 검증하고, 상품 평점 재집계 이벤트를
     * 발행하는 오케스트레이션. 매장 리뷰의 존재 여부는 확인하지 않는다(독립 축).
     */
    @Bean
    public MenuReviewLifecycleService menuReviewLifecycleService(
        MenuReviewRepository menuReviewRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new MenuReviewLifecycleService(menuReviewRepository, domainEventPublisher);
    }
}
