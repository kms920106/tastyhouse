package com.tastyhouse.infrastructure.order.persistence;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 주문 상품 옵션 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class OrderProductOptionMapper {

    private OrderProductOptionMapper() {
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태). 옵션은 update 행위가 없어 insert 전용이다.
     */
    static OrderProductOptionJpaEntity toEntity(OrderProductOption domain) {
        return OrderProductOptionJpaEntity.create(
            IdMapping.raw(domain.getOrderProductId(), OrderProductId::value),
            IdMapping.raw(domain.getOptionGroupId(), ProductOptionGroupId::value),
            domain.getOptionGroupName(),
            IdMapping.raw(domain.getOptionId(), ProductOptionId::value),
            domain.getOptionName(),
            domain.getAdditionalPrice()
        );
    }
}
