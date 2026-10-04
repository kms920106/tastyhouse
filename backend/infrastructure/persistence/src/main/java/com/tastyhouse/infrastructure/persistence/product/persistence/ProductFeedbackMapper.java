package com.tastyhouse.infrastructure.persistence.product.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductFeedbackMapper {

    private ProductFeedbackMapper() {
    }

    static ProductFeedback toDomain(ProductFeedbackJpaEntity entity) {
        return ProductFeedback.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getMemberId() == null ? null : MemberId.of(entity.getMemberId()),
            entity.getFeedbackType() == null ? null : ProductFeedbackType.valueOf(entity.getFeedbackType()),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductFeedbackJpaEntity toEntity(ProductFeedback feedback) {
        return ProductFeedbackJpaEntity.create(
            feedback.getProductId() == null ? null : feedback.getProductId().value(),
            feedback.getShopId() == null ? null : feedback.getShopId().value(),
            feedback.getMemberId() == null ? null : feedback.getMemberId().value(),
            feedback.getFeedbackType() == null ? null : feedback.getFeedbackType().name(),
            feedback.getContent()
        );
    }
}
