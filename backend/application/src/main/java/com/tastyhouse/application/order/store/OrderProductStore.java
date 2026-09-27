package com.tastyhouse.application.order.store;

import java.util.Optional;

import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.application.order.port.out.write.OrderProductStatePort;

public class OrderProductStore implements OrderProductRepository {
    private final OrderProductStatePort orderProductStatePort;

    public OrderProductStore(OrderProductStatePort orderProductStatePort) {
        this.orderProductStatePort = orderProductStatePort;
    }

    @Override
    public Optional<OrderProduct> findById(OrderProductId orderProductId) {
        return orderProductStatePort.findById(orderProductId.value()).map(OrderProductStateMapper::toDomain);
    }

    @Override
    public OrderProduct save(OrderProduct orderProduct) {
        return OrderProductStateMapper.toDomain(orderProductStatePort.save(OrderProductStateMapper.toState(orderProduct)));
    }
}
