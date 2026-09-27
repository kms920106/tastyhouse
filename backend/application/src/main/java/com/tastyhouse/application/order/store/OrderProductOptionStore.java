package com.tastyhouse.application.order.store;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.application.order.port.out.write.OrderProductOptionStatePort;

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
