package com.tastyhouse.core.domain.order.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.order.domain.model.OrderItem;
import com.tastyhouse.core.domain.order.domain.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.order.domain.model.QOrderItem.orderItem;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public Optional<OrderItem> findById(Long orderItemId) {
        return orderItemJpaRepository.findById(orderItemId);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return queryFactory.selectFrom(orderItem)
            .where(orderItem.orderId.eq(orderId))
            .fetch();
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemJpaRepository.save(orderItem);
    }
}
