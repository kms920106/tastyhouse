package com.tastyhouse.infrastructure.order.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.domain.order.repository.OrderProductOptionRepository;

/**
 * 주문 상품 라인 옵션 write 어댑터.
 *
 * <p>write 포트 순수화(공통 지침 패턴 4)로 목록 조회는 {@code order/query/OrderQueryDao}로 이관되어,
 * 이 어댑터는 신규 저장(insert)만 담당한다 — QueryDSL이 필요 없어 {@code JPAQueryFactory}를 주입하지 않는다.
 */
@Repository
public class OrderProductOptionRepositoryImpl implements OrderProductOptionRepository {

    private final OrderProductOptionJpaRepository orderProductOptionJpaRepository;

    public OrderProductOptionRepositoryImpl(OrderProductOptionJpaRepository orderProductOptionJpaRepository) {
        this.orderProductOptionJpaRepository = orderProductOptionJpaRepository;
    }

    @Override
    public void save(OrderProductOption orderProductOption) {
        // 옵션은 update 행위가 없어 신규 저장(insert) 전용이다.
        orderProductOptionJpaRepository.save(OrderProductOptionMapper.toEntity(orderProductOption));
    }
}
