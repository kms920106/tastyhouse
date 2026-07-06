package com.tastyhouse.core.domain.order.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductRepository;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;

import static com.tastyhouse.core.domain.order.domain.model.QOrderProduct.orderProduct;

@Repository
@RequiredArgsConstructor
public class OrderProductRepositoryImpl implements OrderProductRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderProductJpaRepository orderProductJpaRepository;

    @Override
    public Optional<OrderProduct> findById(OrderProductId orderProductId) {
        return orderProductJpaRepository.findById(orderProductId.value());
    }

    @Override
    public List<OrderProduct> findByOrderId(OrderId orderId) {
        return queryFactory.selectFrom(orderProduct)
            .where(orderProduct.orderId.eq(orderId.value()))
            .fetch();
    }

    @Override
    public OrderProduct save(OrderProduct orderProduct) {
        return orderProductJpaRepository.save(orderProduct);
    }
}
