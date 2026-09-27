package com.tastyhouse.application.order.store;

import com.tastyhouse.application.order.port.out.write.OrderProductOptionStatePort;
import com.tastyhouse.domain.order.model.OrderProductOption;

public class OrderProductOptionStore implements OrderProductOptionRepository {
    private final OrderProductOptionStatePort orderProductOptionStatePort;

    public OrderProductOptionStore(OrderProductOptionStatePort orderProductOptionStatePort) {
        this.orderProductOptionStatePort = orderProductOptionStatePort;
    }

    @Override
    public void save(OrderProductOption orderProductOption) {
        orderProductOptionStatePort.save(OrderProductOptionStateMapper.toState(orderProductOption));
    }
}
