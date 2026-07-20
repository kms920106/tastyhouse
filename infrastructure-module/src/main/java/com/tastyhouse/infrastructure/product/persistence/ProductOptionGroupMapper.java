package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;

/**
 * 상품 옵션 그룹 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductOptionGroupMapper {

    private ProductOptionGroupMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductOptionGroup toDomain(ProductOptionGroupJpaEntity entity) {
        return ProductOptionGroup.reconstitute(
            entity.getId(),
            entity.getProductId(),
            entity.getName(),
            entity.getDescription(),
            entity.isRequired(),
            entity.isMultipleSelect(),
            entity.getMinSelect(),
            entity.getMaxSelect(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductOptionGroupJpaEntity toEntity(ProductOptionGroup domain) {
        return ProductOptionGroupJpaEntity.create(
            domain.getProductId(),
            domain.getName(),
            domain.getDescription(),
            domain.isRequired(),
            domain.isMultipleSelect(),
            domain.getMinSelect(),
            domain.getMaxSelect(),
            domain.getSort(),
            domain.isVisible()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ProductOptionGroupJpaEntity entity, ProductOptionGroup domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getDescription(),
            domain.isRequired(),
            domain.isMultipleSelect(),
            domain.getMinSelect(),
            domain.getMaxSelect(),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
