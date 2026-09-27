package com.tastyhouse.infrastructure.order.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.order.port.out.write.OrderProductOptionState;
import com.tastyhouse.application.order.port.out.write.OrderProductOptionStatePort;

@Repository
public class OrderProductOptionStatePortImpl implements OrderProductOptionStatePort {
    private final OrderProductOptionJpaRepository orderProductOptionJpaRepository;

    public OrderProductOptionStatePortImpl(OrderProductOptionJpaRepository orderProductOptionJpaRepository) {
        this.orderProductOptionJpaRepository = orderProductOptionJpaRepository;
    }

    @Override
    public void save(OrderProductOptionState state) {
        orderProductOptionJpaRepository.save(OrderProductOptionMapper.toEntity(state));
    }
}
