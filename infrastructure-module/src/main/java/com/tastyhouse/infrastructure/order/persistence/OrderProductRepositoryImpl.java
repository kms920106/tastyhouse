package com.tastyhouse.infrastructure.order.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.domain.model.OrderProduct;
import com.tastyhouse.domain.order.domain.repository.OrderProductRepository;
import com.tastyhouse.domain.order.domain.vo.OrderProductId;

/**
 * 주문 상품 라인 write 어댑터.
 *
 * <p>write 포트 순수화(공통 지침 패턴 4)로 목록 조회는 {@code order/query/OrderQueryDao}로 이관되어,
 * 이 어댑터는 단건 로드와 저장만 담당한다 — QueryDSL이 필요 없어 {@code JPAQueryFactory}를 주입하지 않는다.
 */
@Repository
@RequiredArgsConstructor
public class OrderProductRepositoryImpl implements OrderProductRepository {

    private final OrderProductJpaRepository orderProductJpaRepository;

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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        OrderProductJpaEntity entity = orderProductJpaRepository.findById(orderProduct.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 주문 상품입니다: " + orderProduct.getId()));
        OrderProductMapper.applyChanges(entity, orderProduct);
        return OrderProductMapper.toDomain(entity);
    }
}
