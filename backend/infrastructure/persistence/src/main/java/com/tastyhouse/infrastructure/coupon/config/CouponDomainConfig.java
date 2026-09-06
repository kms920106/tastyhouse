package com.tastyhouse.infrastructure.coupon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.coupon.repository.CouponRepository;
import com.tastyhouse.domain.coupon.repository.MemberCouponRepository;
import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

@Configuration(proxyBeanMethods = false)
public class CouponDomainConfig {
    @Bean
    public CouponIssueService couponIssueService(
        CouponRepository couponRepository,
        MemberCouponRepository memberCouponRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new CouponIssueService(couponRepository, memberCouponRepository, domainEventPublisher);
    }
}
