package com.tastyhouse.infrastructure.jpa.product.persistence;

import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;

final class StorePriceVerificationItemMapper {

    private StorePriceVerificationItemMapper() {
    }

    static StorePriceVerificationItem toDomain(StorePriceVerificationItemJpaEntity entity) {
        return StorePriceVerificationItem.reconstitute(
            entity.getId(),
            entity.getVerificationId() == null ? null : StorePriceVerificationId.of(entity.getVerificationId()),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
            entity.getProductPriceId() == null ? null : ProductPriceId.of(entity.getProductPriceId()),
            entity.getStorePrice(),
            entity.isApplyPickupSamePrice(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static StorePriceVerificationItemJpaEntity toEntity(StorePriceVerificationItem item) {
        return StorePriceVerificationItemJpaEntity.create(
            item.getVerificationId() == null ? null : item.getVerificationId().value(),
            item.getProductId() == null ? null : item.getProductId().value(),
            item.getProductPriceId() == null ? null : item.getProductPriceId().value(),
            item.getStorePrice(),
            item.isApplyPickupSamePrice()
        );
    }
}
