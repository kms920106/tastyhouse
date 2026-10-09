package com.tastyhouse.infrastructure.jpa.product.persistence;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductFeedbackReadMapper {

    private ProductFeedbackReadMapper() {
    }

    static ProductFeedbackRead toDomain(ProductFeedbackReadJpaEntity entity) {
        return ProductFeedbackRead.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getReadAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductFeedbackReadJpaEntity toEntity(ProductFeedbackRead feedbackRead) {
        return ProductFeedbackReadJpaEntity.create(
            feedbackRead.getShopId() == null ? null : feedbackRead.getShopId().value(),
            feedbackRead.getReadAt()
        );
    }

    static void applyChanges(ProductFeedbackReadJpaEntity entity, ProductFeedbackRead feedbackRead) {
        entity.applyChanges(feedbackRead.getReadAt());
    }
}
