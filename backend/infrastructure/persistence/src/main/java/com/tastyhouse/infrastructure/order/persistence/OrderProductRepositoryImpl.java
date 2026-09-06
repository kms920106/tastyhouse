package com.tastyhouse.infrastructure.order.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.repository.OrderProductRepository;
import com.tastyhouse.domain.order.vo.OrderProductId;

@Repository
public class OrderProductRepositoryImpl implements OrderProductRepository {
    private final OrderProductJpaRepository orderProductJpaRepository;

    public OrderProductRepositoryImpl(OrderProductJpaRepository orderProductJpaRepository) {
        this.orderProductJpaRepository = orderProductJpaRepository;
    }

    @Override
    public Optional<OrderProduct> findById(OrderProductId orderProductId) {
        return orderProductJpaRepository.findById(orderProductId.value()).map(OrderProductMapper::toDomain);
    }

    @Override
    public OrderProduct save(OrderProduct orderProduct) {
        if (orderProduct.getId() == null) {
            OrderProductJpaEntity saved = orderProductJpaRepository.save(OrderProductMapper.toEntity(orderProduct));
            return OrderProductMapper.toDomain(saved);
        }

        OrderProductJpaEntity entity = orderProductJpaRepository.findById(orderProduct.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문 상품입니다: " + orderProduct.getId()));
        OrderProductMapper.applyChanges(entity, orderProduct);
        return OrderProductMapper.toDomain(entity);
    }
}
