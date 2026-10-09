package com.tastyhouse.application.order.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.vo.OrderId;

public interface OrderLoadPort {

    Optional<Order> findById(OrderId orderId);
}
