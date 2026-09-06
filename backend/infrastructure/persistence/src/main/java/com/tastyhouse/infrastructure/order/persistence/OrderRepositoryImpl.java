package com.tastyhouse.infrastructure.order.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderId;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.value()).map(OrderMapper::toDomain);
    }

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            OrderJpaEntity saved = orderJpaRepository.save(OrderMapper.toEntity(order));
            return OrderMapper.toDomain(saved);
        }

        OrderJpaEntity entity = orderJpaRepository.findById(order.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문입니다: " + order.getId()));
        OrderMapper.applyChanges(entity, order);
        return OrderMapper.toDomain(entity);
    }
}
