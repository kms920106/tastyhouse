package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 고객 의견 도메인 모델 ↔ JPA 엔티티 변환기.
 *
 * <p>제보는 수정되지 않는 사실 기록이라 {@code applyChanges}가 없다 — update 경로가 존재하지 않는다.
 */
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
