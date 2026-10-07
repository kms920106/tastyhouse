package com.tastyhouse.application.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.order.port.in.OrderDeleteCommand;
import com.tastyhouse.application.order.port.in.OrderDeleteUseCase;

@Service
@Transactional
class OrderDeleteService implements OrderDeleteUseCase {

    private final OrderTransitionService orderTransitionService;

    public OrderDeleteService(OrderTransitionService orderTransitionService) {
        this.orderTransitionService = orderTransitionService;
    }

    @Override
    public void deleteOrder(OrderDeleteCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        orderTransitionService.delete(orderId);
    }
}
