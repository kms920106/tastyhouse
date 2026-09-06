package com.tastyhouse.domain.order.repository;

import java.util.Optional;

import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderProductId;

public interface OrderProductRepository {
    Optional<OrderProduct> findById(OrderProductId orderProductId);

    OrderProduct save(OrderProduct orderProduct);
}
