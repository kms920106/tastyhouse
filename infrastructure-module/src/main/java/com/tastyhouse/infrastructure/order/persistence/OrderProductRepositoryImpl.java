package com.tastyhouse.infrastructure.order.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.order.domain.model.OrderProduct;
import com.tastyhouse.core.domain.order.domain.repository.OrderProductRepository;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;

import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;

@Repository
@RequiredArgsConstructor
public class OrderProductRepositoryImpl implements OrderProductRepository {

    private final JPAQueryFactory queryFactory;
    private final OrderProductJpaRepository orderProductJpaRepository;

    @Override
    public Optional<OrderProduct> findById(OrderProductId orderProductId) {
        return orderProductJpaRepository.findById(orderProductId.value()).map(OrderProductMapper::toDomain);
    }

    @Override
    public List<OrderProduct> findByOrderId(OrderId orderId) {
        return queryFactory.selectFrom(orderProductJpaEntity)
            .where(orderProductJpaEntity.orderId.eq(orderId.value()))
            .fetch()
            .stream()
            .map(OrderProductMapper::toDomain)
            .toList();
    }

    @Override
    public OrderProduct save(OrderProduct orderProduct) {
        if (orderProduct.getId() == null) {
            OrderProductJpaEntity saved = orderProductJpaRepository.save(OrderProductMapper.toEntity(orderProduct));
            return OrderProductMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        OrderProductJpaEntity entity = orderProductJpaRepository.findById(orderProduct.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문 상품입니다: " + orderProduct.getId()));
        OrderProductMapper.applyChanges(entity, orderProduct);
        return OrderProductMapper.toDomain(entity);
    }
}
