package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductFeedbackMapper {
    private ProductFeedbackMapper() {
    }

    static ProductFeedback toDomain(ProductFeedbackJpaEntity entity) {
        return ProductFeedback.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getFeedbackType(),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductFeedbackJpaEntity toEntity(ProductFeedback domain) {
        return ProductFeedbackJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getFeedbackType(),
            domain.getContent()
        );
    }
}
