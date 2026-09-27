package com.tastyhouse.infrastructure.order.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.order.port.out.write.OrderState;
import com.tastyhouse.application.order.port.out.write.OrderStatePort;

@Repository
public class OrderStatePortImpl implements OrderStatePort {
    private final OrderJpaRepository orderJpaRepository;

    public OrderStatePortImpl(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Optional<OrderState> findById(Long orderId) {
        return orderJpaRepository.findById(orderId).map(OrderMapper::toState);
    }

    @Override
    public OrderState save(OrderState state) {
        if (state.id() == null) {
            OrderJpaEntity saved = orderJpaRepository.save(OrderMapper.toEntity(state));
            return OrderMapper.toState(saved);
        }

        OrderJpaEntity entity = orderJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문입니다: " + state.id()));
        OrderMapper.applyChanges(entity, state);
        return OrderMapper.toState(entity);
    }
}
