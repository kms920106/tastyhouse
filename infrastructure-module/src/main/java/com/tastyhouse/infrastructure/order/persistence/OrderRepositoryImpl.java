package com.tastyhouse.infrastructure.order.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.order.domain.model.Order;
import com.tastyhouse.core.domain.order.domain.repository.OrderRepository;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;

/**
 * 주문 write 어댑터.
 *
 * <p>write 포트 순수화(공통 지침 패턴 4)로 목록·검색 조회(shop/payment Q타입 조인 포함)는 같은 모듈의
 * {@code order/query/OrderQueryDao}로 이관되어, 이 어댑터는 단건 로드와 저장만 담당한다 — QueryDSL이
 * 필요 없어 {@code JPAQueryFactory}를 주입하지 않는다.
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        OrderJpaEntity entity = orderJpaRepository.findById(order.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문입니다: " + order.getId()));
        OrderMapper.applyChanges(entity, order);
        return OrderMapper.toDomain(entity);
    }
}
