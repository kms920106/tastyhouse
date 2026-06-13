package com.tastyhouse.core.domain.order.domain.repository;

import com.tastyhouse.core.domain.order.domain.model.OrderProduct;

import java.util.List;
import java.util.Optional;

public interface OrderProductRepository {

    Optional<OrderProduct> findById(Long orderProductId);

    List<OrderProduct> findByOrderId(Long orderId);

    OrderProduct save(OrderProduct orderProduct);
}
