package com.tastyhouse.application.order.port.out.write;

import java.util.Optional;

public interface OrderStatePort {
    Optional<OrderState> findById(Long orderId);

    OrderState save(OrderState state);
}
