package com.tastyhouse.infrastructure.persistence.order.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.order.port.out.write.OrderPersistencePort;

@Repository
class OrderPersistenceAdapter implements OrderPersistencePort {

    private final OrderJpaRepository orderJpaRepository;

    public OrderPersistenceAdapter(OrderJpaRepository orderJpaRepository) {
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
