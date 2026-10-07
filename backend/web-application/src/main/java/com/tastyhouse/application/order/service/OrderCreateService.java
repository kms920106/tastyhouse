package com.tastyhouse.application.order.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.OrderPlacement;
import com.tastyhouse.domain.order.model.OrderPlacementItem;
import com.tastyhouse.domain.order.model.OrderPlacementItemOption;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.order.port.in.OrderCreateCommand;
import com.tastyhouse.application.order.port.in.OrderCreateUseCase;
import com.tastyhouse.application.order.port.in.OrderLineCommand;

@Service
@Transactional
class OrderCreateService implements OrderCreateUseCase {

    private final OrderPlacementService orderPlacementService;

    public OrderCreateService(OrderPlacementService orderPlacementService) {
        this.orderPlacementService = orderPlacementService;
    }

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
