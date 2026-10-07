package com.tastyhouse.application.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.order.port.in.OrderManagementStatusChangeUseCase;
import com.tastyhouse.application.order.port.in.OrderStatusChangeCommand;

@Service
@Transactional
class OrderManagementStatusChangeService implements OrderManagementStatusChangeUseCase {

    private final OrderTransitionService orderTransitionService;

    public OrderManagementStatusChangeService(OrderTransitionService orderTransitionService) {
        this.orderTransitionService = orderTransitionService;
    }

    @Override
    public void changeStatus(OrderStatusChangeCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        OrderStatus orderStatus = OrderStatus.from(command.status());
        orderTransitionService.changeStatus(orderId, orderStatus);
    }
}
