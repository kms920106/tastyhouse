package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 상품 옵션 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductOptionMapper {

    private ProductOptionMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductOption toDomain(ProductOptionJpaEntity entity) {
        return ProductOption.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getOptionGroupId(), ProductOptionGroupId::of),
            entity.getName(),
            entity.getAdditionalPrice(),
            entity.getSort(),
            entity.isSoldOut(),
            entity.getSoldOutUntil(),
            entity.isVisible(),
            entity.getCupCount(),
            entity.getPersonalCupDiscountAmount()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductOptionJpaEntity toEntity(ProductOption domain) {
        return ProductOptionJpaEntity.create(
            IdMapping.raw(domain.getOptionGroupId(), ProductOptionGroupId::value),
            domain.getName(),
            domain.getAdditionalPrice(),
            domain.getSort(),
            domain.isSoldOut(),
            domain.getSoldOutUntil(),
            domain.isVisible(),
            domain.getCupCount(),
            domain.getPersonalCupDiscountAmount()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ProductOptionJpaEntity entity, ProductOption domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getAdditionalPrice(),
            domain.getSort(),
            domain.isSoldOut(),
            domain.getSoldOutUntil(),
            domain.isVisible(),
            domain.getCupCount(),
            domain.getPersonalCupDiscountAmount()
        );
    }
}
