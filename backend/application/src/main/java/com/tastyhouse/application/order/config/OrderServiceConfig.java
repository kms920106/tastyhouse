package com.tastyhouse.application.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.coupon.service.CouponIssueService;
import com.tastyhouse.application.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.application.member.service.MemberDeliveryAddressService;
import com.tastyhouse.application.member.service.OrdererLookupService;
import com.tastyhouse.application.order.port.out.write.OrderProductOptionStatePort;
import com.tastyhouse.application.order.port.out.write.OrderProductStatePort;
import com.tastyhouse.application.order.port.out.write.OrderStatePort;
import com.tastyhouse.application.order.store.OrderProductOptionRepository;
import com.tastyhouse.application.order.store.OrderProductOptionStore;
import com.tastyhouse.application.order.store.OrderProductRepository;
import com.tastyhouse.application.order.store.OrderProductStore;
import com.tastyhouse.application.order.store.OrderRepository;
import com.tastyhouse.application.order.store.OrderStore;
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
    public OrderRepository orderRepository(OrderStatePort orderStatePort) {
        return new OrderStore(orderStatePort);
    }

    @Bean
    public OrderProductRepository orderProductRepository(OrderProductStatePort orderProductStatePort) {
        return new OrderProductStore(orderProductStatePort);
    }

    @Bean
    public OrderProductOptionRepository orderProductOptionRepository(
        OrderProductOptionStatePort orderProductOptionStatePort
    ) {
        return new OrderProductOptionStore(orderProductOptionStatePort);
    }

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
