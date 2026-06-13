package com.tastyhouse.core.domain.order.domain.repository;

import com.tastyhouse.core.domain.order.domain.model.OrderItemOption;

import java.util.List;

public interface OrderItemOptionRepository {

    List<OrderItemOption> findByOrderItemId(Long orderItemId);

    void save(OrderItemOption orderItemOption);
}
