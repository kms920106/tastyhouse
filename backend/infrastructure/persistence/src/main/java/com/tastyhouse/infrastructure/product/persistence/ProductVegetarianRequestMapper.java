package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 메뉴 채식 설정 승인요청 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ProductVegetarianRequestMapper {

    private ProductVegetarianRequestMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ProductVegetarianRequest toDomain(ProductVegetarianRequestJpaEntity entity) {
        return ProductVegetarianRequest.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getVegetarianType(),
            entity.getIngredients(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getRejectReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ProductVegetarianRequestJpaEntity toEntity(ProductVegetarianRequest domain) {
        return ProductVegetarianRequestJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getVegetarianType(),
            domain.getIngredients(),
            domain.getDescription(),
            domain.getStatus(),
            domain.getRejectReason()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ProductVegetarianRequestJpaEntity entity, ProductVegetarianRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.getRejectReason()
        );
    }
}
