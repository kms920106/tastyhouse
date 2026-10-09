package com.tastyhouse.infrastructure.jpa.order.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.application.order.port.out.write.OrderProductOptionSavePort;

@Repository
class OrderProductOptionPersistenceAdapter implements OrderProductOptionSavePort {

    private final OrderProductOptionJpaRepository orderProductOptionJpaRepository;

    public OrderProductOptionPersistenceAdapter(OrderProductOptionJpaRepository orderProductOptionJpaRepository) {
        this.orderProductOptionJpaRepository = orderProductOptionJpaRepository;
    }

    @Override
    public void save(OrderProductOption orderProductOption) {
        orderProductOptionJpaRepository.save(OrderProductOptionMapper.toEntity(orderProductOption));
    }
}
