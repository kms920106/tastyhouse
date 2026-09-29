package com.tastyhouse.application.coupon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.coupon.port.out.write.CouponPersistencePort;
import com.tastyhouse.application.coupon.port.out.write.MemberCouponPersistencePort;
import com.tastyhouse.application.coupon.service.CouponIssueService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class CouponServiceConfig {
    @Bean
    public CouponIssueService couponIssueService(
        CouponPersistencePort couponPersistencePort,
        MemberCouponPersistencePort memberCouponPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new CouponIssueService(couponPersistencePort, memberCouponPersistencePort, domainEventPublisher);
    }
}
