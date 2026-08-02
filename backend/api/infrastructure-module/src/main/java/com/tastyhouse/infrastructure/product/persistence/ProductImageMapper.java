package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductImage;

/**
 * 상품 이미지 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductImageMapper {

    private ProductImageMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductImage toDomain(ProductImageJpaEntity entity) {
        return ProductImage.reconstitute(
            entity.getId(),
            entity.getProductId(),
            entity.getImageFileId(),
            entity.getSort(),
            entity.isVisible()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductImageJpaEntity toEntity(ProductImage domain) {
        return ProductImageJpaEntity.create(
            domain.getProductId(),
            domain.getImageFileId(),
            domain.getSort(),
            domain.isVisible()
        );
    }
}
