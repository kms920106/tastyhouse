package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.order.domain.model.OrderProductOption;

/**
 * 주문 상품 옵션 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class OrderProductOptionMapper {

    private OrderProductOptionMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static OrderProductOption toDomain(OrderProductOptionJpaEntity entity) {
        return OrderProductOption.reconstitute(
            entity.getId(),
            entity.getOrderProductId(),
            entity.getOptionGroupId(),
            entity.getOptionGroupName(),
            entity.getOptionId(),
            entity.getOptionName(),
            entity.getAdditionalPrice()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태). 옵션은 update 행위가 없어 insert 전용이다.
     */
    static OrderProductOptionJpaEntity toEntity(OrderProductOption domain) {
        return OrderProductOptionJpaEntity.create(
            domain.getOrderProductId(),
            domain.getOptionGroupId(),
            domain.getOptionGroupName(),
            domain.getOptionId(),
            domain.getOptionName(),
            domain.getAdditionalPrice()
        );
    }
}
