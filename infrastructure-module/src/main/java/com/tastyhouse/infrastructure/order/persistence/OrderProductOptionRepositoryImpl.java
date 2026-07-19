package com.tastyhouse.infrastructure.order.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.order.domain.model.OrderProductOption;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductOptionRepository;
import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;

import static com.tastyhouse.infrastructure.order.persistence.QOrderProductOptionJpaEntity.orderProductOptionJpaEntity;

@Repository
@RequiredArgsConstructor
public class OrderProductOptionRepositoryImpl implements OrderProductOptionRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderProductOptionJpaRepository orderProductOptionJpaRepository;

    @Override
    public List<OrderProductOption> findByOrderProductId(OrderProductId orderProductId) {
        return queryFactory.selectFrom(orderProductOptionJpaEntity)
            .where(orderProductOptionJpaEntity.orderProductId.eq(orderProductId.value()))
            .fetch()
            .stream()
            .map(OrderProductOptionMapper::toDomain)
            .toList();
    }

    @Override
    public void save(OrderProductOption orderProductOption) {
        // 옵션은 update 행위가 없어 신규 저장(insert) 전용이다.
        orderProductOptionJpaRepository.save(OrderProductOptionMapper.toEntity(orderProductOption));
    }
}
