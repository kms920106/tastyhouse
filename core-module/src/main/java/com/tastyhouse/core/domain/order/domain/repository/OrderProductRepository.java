package com.tastyhouse.core.domain.order.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;

public interface OrderProductRepository {

    Optional<OrderProduct> findById(OrderProductId orderProductId);

    List<OrderProduct> findByOrderId(OrderId orderId);

    OrderProduct save(OrderProduct orderProduct);
}
