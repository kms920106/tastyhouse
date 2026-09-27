package com.tastyhouse.application.order.port.out.write;

import java.util.Optional;

public interface OrderProductStatePort {
    Optional<OrderProductState> findById(Long orderProductId);

    OrderProductState save(OrderProductState state);
}
