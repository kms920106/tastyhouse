package com.tastyhouse.application.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.coupon.service.CouponIssueService;
import com.tastyhouse.application.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.application.member.service.MemberDeliveryAddressService;
import com.tastyhouse.application.member.service.OrdererLookupService;
import com.tastyhouse.application.order.port.out.write.OrderPersistencePort;
import com.tastyhouse.application.order.port.out.write.OrderProductOptionPersistencePort;
import com.tastyhouse.application.order.port.out.write.OrderProductPersistencePort;
import com.tastyhouse.application.order.service.OrderPlacementService;
import com.tastyhouse.application.order.service.OrderTransitionService;
import com.tastyhouse.application.point.service.PointLedgerService;
import com.tastyhouse.application.product.service.OrderProductValidationService;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.service.ShopOrderContextService;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class OrderServiceConfig {

    @Bean
    public OrderPlacementService orderPlacementService(
        OrderPersistencePort orderPersistencePort,
        OrderProductPersistencePort orderProductPersistencePort,
        OrderProductOptionPersistencePort orderProductOptionPersistencePort,
        OrderProductValidationService orderProductValidationService,
        ShopOrderContextService shopOrderContextService,
        OrdererLookupService ordererLookupService,
        MemberDeliveryAddressService memberDeliveryAddressService,
        CouponIssueService couponIssueService,
        PointLedgerService pointLedgerService,
        PublicHolidayCalendar publicHolidayCalendar
    ) {
        return new OrderPlacementService(
            orderPersistencePort,
            orderProductPersistencePort,
            orderProductOptionPersistencePort,
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
    public OrderTransitionService orderTransitionService(OrderPersistencePort orderPersistencePort) {
        return new OrderTransitionService(orderPersistencePort);
    }
}
