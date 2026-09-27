package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadState;

final class ProductFeedbackReadMapper {
    private ProductFeedbackReadMapper() {
    }

    static ProductFeedbackReadState toState(ProductFeedbackReadJpaEntity entity) {
        return new ProductFeedbackReadState(
            entity.getId(),
            entity.getShopId(),
            entity.getReadAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductFeedbackReadJpaEntity toEntity(ProductFeedbackReadState state) {
        return ProductFeedbackReadJpaEntity.create(
            state.shopId(),
            state.readAt()
        );
    }

    static void applyChanges(ProductFeedbackReadJpaEntity entity, ProductFeedbackReadState state) {
        entity.applyChanges(state.readAt());
    }
}
