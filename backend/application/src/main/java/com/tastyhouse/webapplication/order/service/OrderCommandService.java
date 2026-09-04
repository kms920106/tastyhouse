package com.tastyhouse.webapplication.order.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.service.OrderPlacement;
import com.tastyhouse.domain.order.service.OrderPlacementItem;
import com.tastyhouse.domain.order.service.OrderPlacementItemOption;
import com.tastyhouse.domain.order.service.OrderPlacementService;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.webapplication.order.port.in.OrderCommandUseCase;
import com.tastyhouse.webapplication.order.port.in.OrderCreateCommand;
import com.tastyhouse.webapplication.order.port.in.OrderLineCommand;

/**
 * 회원 주문 command 서비스(web-api).
 *
 * <p>인바운드 포트로 받은 {@link OrderCreateCommand}를 도메인 입력({@link OrderPlacement})으로 조립하고, 트랜잭션 경계를
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
public class OrderCommandService implements OrderCommandUseCase {

    private final OrderPlacementService orderPlacementService;

    public OrderCommandService(OrderPlacementService orderPlacementService) {
        this.orderPlacementService = orderPlacementService;
    }

    /**
     * 주문을 생성한다.
     */
    @Override
    public Long createOrder(OrderCreateCommand command) {
        MemberId memberIdVo = MemberId.of(command.memberId());
        OrderPlacement placement = toPlacement(
            command.shopId(),
            OrderMethod.from(command.orderMethod()),
            command.orderLines(),
            command.memberCouponId(),
            command.usePoint(),
            command.deliveryAddressId(),
            command.totalProductAmount(),
            command.totalDiscountAmount(),
            command.productDiscountAmount(),
            command.couponDiscountAmount(),
            command.deliveryTipAmount(),
            command.cupDepositAmount(),
            command.finalAmount(),
            command.scheduledAt()
        );
        OrderId orderId = orderPlacementService.place(memberIdVo, placement);
        return orderId.value();
    }

    private OrderPlacement toPlacement(
        Long shopId,
        OrderMethod orderMethod,
        List<OrderLineCommand> orderLines,
        Long memberCouponId,
        Integer usePoint,
        Long deliveryAddressId,
        Integer totalProductAmount,
        Integer totalDiscountAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer deliveryTipAmount,
        Integer cupDepositAmount,
        Integer finalAmount,
        LocalDateTime scheduledAt
    ) {
        List<OrderPlacementItem> items = orderLines.stream()
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
            cupDepositAmount,
            finalAmount,
            scheduledAt
        );
    }

    private OrderPlacementItem toPlacementItem(OrderLineCommand line) {
        List<OrderPlacementItemOption> options = line.options() == null ? null :
            line.options().stream()
                .map(option -> OrderPlacementItemOption.of(option.groupId(), option.optionId()))
                .toList();
        return OrderPlacementItem.of(line.productId(), line.priceId(), line.quantity(), options);
    }
}
