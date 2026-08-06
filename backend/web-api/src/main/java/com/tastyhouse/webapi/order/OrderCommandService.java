package com.tastyhouse.webapi.order;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.service.OrderPlacement;
import com.tastyhouse.domain.order.service.OrderPlacementItem;
import com.tastyhouse.domain.order.service.OrderPlacementItemOption;
import com.tastyhouse.domain.order.service.OrderPlacementService;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.webapi.order.request.OrderProductRequest;

/**
 * 회원 주문 command 서비스(web-api).
 *
 * <p>HTTP 경계에서 받은 원시 파라미터를 도메인 입력({@link OrderPlacement})으로 조립하고, 트랜잭션 경계를
 * 선언해 도메인 서비스 {@link OrderPlacementService}에 위임한다(공통 지침 패턴 2). 주문 접수의 불변식
 * (세 애그리거트 원자 생성·금액 계산·쿠폰/포인트 연동)은 도메인 서비스가 갖고, 이 서비스는 조립과 경계만
 * 담당한다.
 *
 * <p>{@code Long → MemberId} 승격과 {@code String → OrderMethod} 승격은 기존 경계 규칙대로 여기서 한다.
 *
 * <p>반환은 생성된 주문 식별자({@code Long})다 — 응답 조립은 커밋 이후 {@link OrderQueryService}가
 * 재조회해 담당한다(CQRS 분리).
 */
@Service
@Transactional
public class OrderCommandService {

    private final OrderPlacementService orderPlacementService;

    public OrderCommandService(OrderPlacementService orderPlacementService) {
        this.orderPlacementService = orderPlacementService;
    }

    /**
     * 주문을 생성한다.
     */
    public Long createOrder(
        Long memberId,
        Long shopId,
        String orderMethod,
        List<OrderProductRequest> orderProducts,
        Long memberCouponId,
        Integer usePoint,
        Long deliveryAddressId,
        Integer totalProductAmount,
        Integer totalDiscountAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer deliveryTipAmount,
        Integer finalAmount
    ) {
        MemberId memberIdVo = MemberId.of(memberId);
        OrderPlacement placement = toPlacement(
            shopId,
            OrderMethod.from(orderMethod),
            orderProducts,
            memberCouponId,
            usePoint,
            deliveryAddressId,
            totalProductAmount,
            totalDiscountAmount,
            productDiscountAmount,
            couponDiscountAmount,
            deliveryTipAmount,
            finalAmount
        );
        OrderId orderId = orderPlacementService.place(memberIdVo, placement);
        return orderId.value();
    }

    private OrderPlacement toPlacement(
        Long shopId,
        OrderMethod orderMethod,
        List<OrderProductRequest> orderProducts,
        Long memberCouponId,
        Integer usePoint,
        Long deliveryAddressId,
        Integer totalProductAmount,
        Integer totalDiscountAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer deliveryTipAmount,
        Integer finalAmount
    ) {
        List<OrderPlacementItem> items = orderProducts.stream()
            .map(this::toPlacementItem)
            .toList();
        return OrderPlacement.of(
            shopId,
            orderMethod,
            items,
            memberCouponId,
            usePoint,
            deliveryAddressId,
            totalProductAmount,
            totalDiscountAmount,
            productDiscountAmount,
            couponDiscountAmount,
            deliveryTipAmount,
            finalAmount
        );
    }

    private OrderPlacementItem toPlacementItem(OrderProductRequest request) {
        List<OrderPlacementItemOption> options = request.options() == null ? null :
            request.options().stream()
                .map(option -> OrderPlacementItemOption.of(option.groupId(), option.optionId()))
                .toList();
        return OrderPlacementItem.of(request.productId(), request.quantity(), options);
    }
}
