package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 메뉴 알레르기 성분 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductAllergenMapper {

    private ProductAllergenMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductAllergen toDomain(ProductAllergenJpaEntity entity) {
        return ProductAllergen.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getAllergenType(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductAllergenJpaEntity toEntity(ProductAllergen domain) {
        return ProductAllergenJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getAllergenType()
        );
    }
}
