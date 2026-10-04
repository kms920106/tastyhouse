package com.tastyhouse.infrastructure.persistence.order.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.application.order.port.out.write.OrderProductOptionPersistencePort;

@Repository
public class OrderProductOptionPersistenceAdapter implements OrderProductOptionPersistencePort {

    private final OrderProductOptionJpaRepository orderProductOptionJpaRepository;

    public OrderProductOptionPersistenceAdapter(OrderProductOptionJpaRepository orderProductOptionJpaRepository) {
        this.orderProductOptionJpaRepository = orderProductOptionJpaRepository;
    }

    @Override
    public void save(OrderProductOption orderProductOption) {
        orderProductOptionJpaRepository.save(OrderProductOptionMapper.toEntity(orderProductOption));
    }
}
