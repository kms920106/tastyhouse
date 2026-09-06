package com.tastyhouse.infrastructure.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.member.service.MemberDeliveryAddressService;
import com.tastyhouse.domain.member.service.OrdererLookupService;
import com.tastyhouse.domain.order.repository.OrderProductOptionRepository;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.service.OrderPlacementService;
import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.point.service.PointLedgerService;
import com.tastyhouse.domain.product.service.OrderProductValidationService;
import com.tastyhouse.domain.shop.service.ShopOrderContextService;

@Configuration(proxyBeanMethods = false)
public class OrderDomainConfig {
    @Bean
    public OrderPlacementService orderPlacementService(
        OrderRepository orderRepository,
        OrderProductRepository orderProductRepository,
        OrderProductOptionRepository orderProductOptionRepository,
        OrderProductValidationService orderProductValidationService,
        ShopOrderContextService shopOrderContextService,
        OrdererLookupService ordererLookupService,
        MemberDeliveryAddressService memberDeliveryAddressService,
        CouponIssueService couponIssueService,
        PointLedgerService pointLedgerService,
        PublicHolidayCalendar publicHolidayCalendar
    ) {
        return new OrderPlacementService(
            orderRepository,
            orderProductRepository,
            orderProductOptionRepository,
            orderProductValidationService,
            shopOrderContextService,
            ordererLookupService,
            memberDeliveryAddressService,
            couponIssueService,
            pointLedgerService,
            publicHolidayCalendar
        );
    }

    @Bean
    public OrderTransitionService orderTransitionService(OrderRepository orderRepository) {
        return new OrderTransitionService(orderRepository);
    }
}
