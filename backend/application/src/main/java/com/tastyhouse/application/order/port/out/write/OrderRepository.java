package com.tastyhouse.application.order.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.vo.OrderId;

public interface OrderRepository {
    Optional<Order> findById(OrderId orderId);

    Order save(Order order);
}
