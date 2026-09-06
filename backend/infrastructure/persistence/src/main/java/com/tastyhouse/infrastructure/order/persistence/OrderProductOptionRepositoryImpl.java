package com.tastyhouse.infrastructure.order.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.domain.order.repository.OrderProductOptionRepository;

@Repository
public class OrderProductOptionRepositoryImpl implements OrderProductOptionRepository {
    private final OrderProductOptionJpaRepository orderProductOptionJpaRepository;

    public OrderProductOptionRepositoryImpl(OrderProductOptionJpaRepository orderProductOptionJpaRepository) {
        this.orderProductOptionJpaRepository = orderProductOptionJpaRepository;
    }

    @Override
    public void save(OrderProductOption orderProductOption) {
        orderProductOptionJpaRepository.save(OrderProductOptionMapper.toEntity(orderProductOption));
    }
}
