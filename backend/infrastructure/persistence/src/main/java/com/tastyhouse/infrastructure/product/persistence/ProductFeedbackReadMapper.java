package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 점주 의견 확인 시각 도메인 모델 ↔ JPA 엔티티 변환기.
 */
final class ProductFeedbackReadMapper {

    private ProductFeedbackReadMapper() {
    }

    static ProductFeedbackRead toDomain(ProductFeedbackReadJpaEntity entity) {
        return ProductFeedbackRead.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getReadAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductFeedbackReadJpaEntity toEntity(ProductFeedbackRead domain) {
        return ProductFeedbackReadJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getReadAt()
        );
    }

    static void applyChanges(ProductFeedbackReadJpaEntity entity, ProductFeedbackRead domain) {
        entity.applyChanges(domain.getReadAt());
    }
}
