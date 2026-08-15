package com.tastyhouse.infrastructure.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.order.repository.OrderProductOptionRepository;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.service.OrderPlacementService;
import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.point.service.PointLedgerService;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotService;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopOrderAvailabilityService;

/**
 * order 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class OrderDomainConfig {

    /**
     * 주문 접수 — 주문 헤더·상품 라인·라인 옵션 세 애그리거트를 한 트랜잭션에서 함께 만들고, 금액 계산과
     * 쿠폰 사용·포인트 차감까지 원자로 묶는 오케스트레이션.
     */
    @Bean
    public OrderPlacementService orderPlacementService(
        OrderRepository orderRepository,
        OrderProductRepository orderProductRepository,
        OrderProductOptionRepository orderProductOptionRepository,
        ShopRepository shopRepository,
        MemberRepository memberRepository,
        ProductRepository productRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        CouponIssueService couponIssueService,
        PointLedgerService pointLedgerService,
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        MemberDeliveryAddressRepository memberDeliveryAddressRepository,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        PublicHolidayCalendar publicHolidayCalendar,
        ScheduledOrderSlotService scheduledOrderSlotService,
        ShopOrderAvailabilityService shopOrderAvailabilityService
    ) {
        return new OrderPlacementService(
            orderRepository,
            orderProductRepository,
            orderProductOptionRepository,
            shopRepository,
            memberRepository,
            productRepository,
            productOptionGroupRepository,
            productOptionRepository,
            productImageRepository,
            couponIssueService,
            pointLedgerService,
            shopDeliveryTipRepository,
            shopDeliveryAreaRepository,
            memberDeliveryAddressRepository,
            shopDeliveryTipCalculator,
            publicHolidayCalendar,
            scheduledOrderSlotService,
            shopOrderAvailabilityService
        );
    }

    /**
     * 주문 상태전이 — 결제·포인트 연쇄의 진입점. 주문 로드·전이·저장을 원자로 묶어, 트리거 액터
     * (회원·관리자·결제 콜백)가 여러 개여도 전이 규칙이 갈리지 않게 한다.
     */
    @Bean
    public OrderTransitionService orderTransitionService(OrderRepository orderRepository) {
        return new OrderTransitionService(orderRepository);
    }
}
