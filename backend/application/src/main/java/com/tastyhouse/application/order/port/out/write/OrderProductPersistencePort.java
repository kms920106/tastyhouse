package com.tastyhouse.application.order.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderProductId;

public interface OrderProductPersistencePort {

    Optional<OrderProduct> findById(OrderProductId orderProductId);

    OrderProduct save(OrderProduct orderProduct);
}
