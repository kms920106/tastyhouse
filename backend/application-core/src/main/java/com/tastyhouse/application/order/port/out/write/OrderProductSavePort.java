package com.tastyhouse.application.order.port.out.write;

import com.tastyhouse.domain.order.model.OrderProduct;

public interface OrderProductSavePort {

    OrderProduct save(OrderProduct orderProduct);
}
