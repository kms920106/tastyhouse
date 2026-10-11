package com.tastyhouse.application.order.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderProductId;

public interface OrderProductLoadPort {

    Optional<OrderProduct> findById(OrderProductId orderProductId);
}
