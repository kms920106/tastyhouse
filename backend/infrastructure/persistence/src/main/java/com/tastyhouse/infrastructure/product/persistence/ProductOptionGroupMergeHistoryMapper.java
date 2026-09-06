package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductOptionGroupMergeHistoryMapper {
    private ProductOptionGroupMergeHistoryMapper() {
    }

    static ProductOptionGroupMergeHistory toDomain(ProductOptionGroupMergeHistoryJpaEntity entity) {
        return ProductOptionGroupMergeHistory.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            IdMapping.vo(entity.getBaseOptionGroupId(), ProductOptionGroupId::of),
            IdMapping.vo(entity.getMergedOptionGroupId(), ProductOptionGroupId::of),
            entity.getMergedGroupName(),
            entity.getEntryType(),
            IdMapping.vo(entity.getActorCeoId(), CeoId::of)
        );
    }

    static ProductOptionGroupMergeHistoryJpaEntity toEntity(ProductOptionGroupMergeHistory domain) {
        return ProductOptionGroupMergeHistoryJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            IdMapping.raw(domain.getBaseOptionGroupId(), ProductOptionGroupId::value),
            IdMapping.raw(domain.getMergedOptionGroupId(), ProductOptionGroupId::value),
            domain.getMergedGroupName(),
            domain.getEntryType(),
            IdMapping.raw(domain.getActorCeoId(), CeoId::value)
        );
    }
}
