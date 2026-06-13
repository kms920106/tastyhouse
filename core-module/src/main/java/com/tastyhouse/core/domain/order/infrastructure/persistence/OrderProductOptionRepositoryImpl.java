package com.tastyhouse.core.domain.order.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.order.domain.model.QOrderProductOption.orderProductOption;

@Repository
@RequiredArgsConstructor
public class OrderProductOptionRepositoryImpl implements OrderProductOptionRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderProductOptionJpaRepository orderProductOptionJpaRepository;

    @Override
    public List<OrderProductOption> findByOrderProductId(Long orderProductId) {
        return queryFactory.selectFrom(orderProductOption)
            .where(orderProductOption.orderProductId.eq(orderProductId))
            .fetch();
    }

    @Override
    public void save(OrderProductOption orderProductOption) {
        orderProductOptionJpaRepository.save(orderProductOption);
    }
}
