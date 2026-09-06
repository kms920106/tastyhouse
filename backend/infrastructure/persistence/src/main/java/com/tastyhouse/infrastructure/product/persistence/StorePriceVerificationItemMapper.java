package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class StorePriceVerificationItemMapper {
    private StorePriceVerificationItemMapper() {
    }

    static StorePriceVerificationItem toDomain(StorePriceVerificationItemJpaEntity entity) {
        return StorePriceVerificationItem.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getVerificationId(), StorePriceVerificationId::of),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            IdMapping.vo(entity.getProductPriceId(), ProductPriceId::of),
            entity.getStorePrice(),
            entity.isApplyPickupSamePrice(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static StorePriceVerificationItemJpaEntity toEntity(StorePriceVerificationItem domain) {
        return StorePriceVerificationItemJpaEntity.create(
            IdMapping.raw(domain.getVerificationId(), StorePriceVerificationId::value),
            IdMapping.raw(domain.getProductId(), ProductId::value),
            IdMapping.raw(domain.getProductPriceId(), ProductPriceId::value),
            domain.getStorePrice(),
            domain.isApplyPickupSamePrice()
        );
    }
}
