package com.tastyhouse.infrastructure.order.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.order.port.out.write.OrderProductState;
import com.tastyhouse.application.order.port.out.write.OrderProductStatePort;

@Repository
public class OrderProductStatePortImpl implements OrderProductStatePort {
    private final OrderProductJpaRepository orderProductJpaRepository;

    public OrderProductStatePortImpl(OrderProductJpaRepository orderProductJpaRepository) {
        this.orderProductJpaRepository = orderProductJpaRepository;
    }

    @Override
    public Optional<OrderProductState> findById(Long orderProductId) {
        return orderProductJpaRepository.findById(orderProductId).map(OrderProductMapper::toState);
    }

    @Override
    public OrderProductState save(OrderProductState state) {
        if (state.id() == null) {
            OrderProductJpaEntity saved = orderProductJpaRepository.save(OrderProductMapper.toEntity(state));
            return OrderProductMapper.toState(saved);
        }

        OrderProductJpaEntity entity = orderProductJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문 상품입니다: " + state.id()));
        OrderProductMapper.applyChanges(entity, state);
        return OrderProductMapper.toState(entity);
    }
}
