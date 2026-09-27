package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductFeedbackState;

final class ProductFeedbackMapper {
    private ProductFeedbackMapper() {
    }

    static ProductFeedbackState toState(ProductFeedbackJpaEntity entity) {
        return new ProductFeedbackState(
            entity.getId(),
            entity.getProductId(),
            entity.getShopId(),
            entity.getMemberId(),
            entity.getFeedbackType(),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductFeedbackJpaEntity toEntity(ProductFeedbackState state) {
        return ProductFeedbackJpaEntity.create(
            state.productId(),
            state.shopId(),
            state.memberId(),
            state.feedbackType(),
            state.content()
        );
    }
}
