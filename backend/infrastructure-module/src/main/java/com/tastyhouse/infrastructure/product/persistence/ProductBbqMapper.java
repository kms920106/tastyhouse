package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * Product-BBQ 매핑 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductBbqMapper {

    private ProductBbqMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductBbq toDomain(ProductBbqJpaEntity entity) {
        return ProductBbq.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getBbqMenuId(), BbqMenuId::of),
            IdMapping.vo(entity.getBbqCategoryId(), BbqCategoryId::of),
            entity.isOptionsSynced()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductBbqJpaEntity toEntity(ProductBbq domain) {
        return ProductBbqJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getBbqMenuId(), BbqMenuId::value),
            IdMapping.raw(domain.getBbqCategoryId(), BbqCategoryId::value),
            domain.isOptionsSynced()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ProductBbqJpaEntity entity, ProductBbq domain) {
        entity.applyChanges(domain.isOptionsSynced());
    }
}
