package com.tastyhouse.application.coupon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.coupon.port.out.write.CouponRepository;
import com.tastyhouse.application.coupon.port.out.write.MemberCouponRepository;
import com.tastyhouse.application.coupon.service.CouponIssueService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class CouponServiceConfig {
    @Bean
    public CouponIssueService couponIssueService(
        CouponRepository couponRepository,
        MemberCouponRepository memberCouponRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new CouponIssueService(couponRepository, memberCouponRepository, domainEventPublisher);
    }
}
