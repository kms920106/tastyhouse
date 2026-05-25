package com.tastyhouse.core.domain.order.domain.repository;

import com.tastyhouse.core.domain.order.domain.model.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {

    Optional<OrderItem> findById(Long orderItemId);

    List<OrderItem> findByOrderId(Long orderId);

    OrderItem save(OrderItem orderItem);
}
