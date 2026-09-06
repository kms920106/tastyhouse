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

@Service
@AdminApp
@Transactional
public class OrderManagementCommandService implements OrderManagementCommandUseCase {

    private final OrderTransitionService orderTransitionService;

    public OrderManagementCommandService(OrderTransitionService orderTransitionService) {
        this.orderTransitionService = orderTransitionService;
    }

    @Override
    public void changeStatus(OrderStatusChangeCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        OrderStatus orderStatus = OrderStatus.from(command.status());
        orderTransitionService.changeStatus(orderId, orderStatus);
    }

    @Override
    public void deleteOrder(OrderDeleteCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        orderTransitionService.delete(orderId);
    }
}
