package com.tastyhouse.application.coupon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.coupon.port.out.write.CouponStatePort;
import com.tastyhouse.application.coupon.port.out.write.MemberCouponStatePort;
import com.tastyhouse.application.coupon.store.CouponRepository;
import com.tastyhouse.application.coupon.store.CouponStore;
import com.tastyhouse.application.coupon.store.MemberCouponRepository;
import com.tastyhouse.application.coupon.store.MemberCouponStore;
import com.tastyhouse.application.coupon.service.CouponIssueService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class CouponServiceConfig {
    @Bean
    public CouponRepository couponRepository(CouponStatePort couponStatePort) {
        return new CouponStore(couponStatePort);
    }

    @Bean
    public MemberCouponRepository memberCouponRepository(MemberCouponStatePort memberCouponStatePort) {
        return new MemberCouponStore(memberCouponStatePort);
    }

    @Bean
    public CouponIssueService couponIssueService(
        CouponRepository couponRepository,
        MemberCouponRepository memberCouponRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new CouponIssueService(couponRepository, memberCouponRepository, domainEventPublisher);
    }
}
