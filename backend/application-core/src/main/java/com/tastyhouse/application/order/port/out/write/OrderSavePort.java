package com.tastyhouse.application.order.port.out.write;

import com.tastyhouse.domain.order.model.Order;

public interface OrderSavePort {

    Order save(Order order);
}
