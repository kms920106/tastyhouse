package com.tastyhouse.application.order.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.order.port.in.OrderManagementCommandUseCase;
import com.tastyhouse.application.order.port.in.OrderDeleteCommand;
import com.tastyhouse.application.order.port.in.OrderStatusChangeCommand;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.service.OrderTransitionService;
import com.tastyhouse.domain.order.vo.OrderId;

/**
 * 주문 관리 command 서비스(admin-api).
 *
 * <p>관리자의 주문 상태 변경·삭제를 트랜잭션 경계 안에서 도메인 서비스
 * {@link OrderTransitionService}에 위임한다(공통 지침 패턴 2). 상태 전이 규칙과 저장 책임은 도메인
 * 서비스가 갖고, 이 서비스는 {@code Long → OrderId}·{@code String → OrderStatus} 승격과 경계만 담당한다.
 */
@Service
@AdminApp
@Transactional
public class OrderManagementCommandService implements OrderManagementCommandUseCase {

    private final OrderTransitionService orderTransitionService;

    public OrderManagementCommandService(OrderTransitionService orderTransitionService) {
        this.orderTransitionService = orderTransitionService;
    }

    /**
     * 주문 상태를 변경한다.
     */
    @Override
    public void changeStatus(OrderStatusChangeCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        OrderStatus orderStatus = OrderStatus.from(command.status());
        orderTransitionService.changeStatus(orderId, orderStatus);
    }

    /**
     * 주문을 삭제한다.
     */
    @Override
    public void deleteOrder(OrderDeleteCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        orderTransitionService.delete(orderId);
    }
}
