package com.tastyhouse.application.order.store;

import java.util.Optional;

import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.order.port.out.write.OrderStatePort;

public class OrderStore implements OrderRepository {
    private final OrderStatePort orderStatePort;

    public OrderStore(OrderStatePort orderStatePort) {
        this.orderStatePort = orderStatePort;
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderStatePort.findById(orderId.value()).map(OrderStateMapper::toDomain);
    }

    @Override
    public Order save(Order order) {
        return OrderStateMapper.toDomain(orderStatePort.save(OrderStateMapper.toState(order)));
    }
}
