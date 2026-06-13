package com.tastyhouse.core.domain.order.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.order.domain.model.OrderItemOption;
import com.tastyhouse.core.domain.order.domain.repository.OrderItemOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.order.domain.model.QOrderItemOption.orderItemOption;

@Repository
@RequiredArgsConstructor
public class OrderItemOptionRepositoryImpl implements OrderItemOptionRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderItemOptionJpaRepository orderItemOptionJpaRepository;

    @Override
    public List<OrderItemOption> findByOrderItemId(Long orderItemId) {
        return queryFactory.selectFrom(orderItemOption)
            .where(orderItemOption.orderItemId.eq(orderItemId))
            .fetch();
    }

    @Override
    public void save(OrderItemOption orderItemOption) {
        orderItemOptionJpaRepository.save(orderItemOption);
    }
}
