package com.tastyhouse.infrastructure.coupon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.coupon.repository.CouponRepository;
import com.tastyhouse.domain.coupon.repository.MemberCouponRepository;
import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * coupon 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class CouponDomainConfig {

    /**
     * 쿠폰 발급·사용 — 쿠폰 원본 정책과 회원 보유분 두 애그리거트를 한 트랜잭션에서 함께 다루는 오케스트레이션.
     */
    @Bean
    public CouponIssueService couponIssueService(
        CouponRepository couponRepository,
        MemberCouponRepository memberCouponRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new CouponIssueService(couponRepository, memberCouponRepository, domainEventPublisher);
    }
}
