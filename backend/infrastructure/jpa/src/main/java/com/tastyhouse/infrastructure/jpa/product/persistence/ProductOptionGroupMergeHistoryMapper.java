package com.tastyhouse.infrastructure.jpa.product.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductOptionGroupMergeHistoryMapper {

    private ProductOptionGroupMergeHistoryMapper() {
    }

    static ProductOptionGroupMergeHistory toDomain(ProductOptionGroupMergeHistoryJpaEntity entity) {
        return ProductOptionGroupMergeHistory.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getBaseOptionGroupId() == null ? null : ProductOptionGroupId.of(entity.getBaseOptionGroupId()),
            entity.getMergedOptionGroupId() == null ? null : ProductOptionGroupId.of(entity.getMergedOptionGroupId()),
            entity.getMergedGroupName(),
            entity.getEntryType() == null ? null : ProductOptionGroupMergeEntryType.valueOf(entity.getEntryType()),
            entity.getActorCeoId() == null ? null : CeoId.of(entity.getActorCeoId())
        );
    }

    static ProductOptionGroupMergeHistoryJpaEntity toEntity(ProductOptionGroupMergeHistory history) {
        return ProductOptionGroupMergeHistoryJpaEntity.create(
            history.getShopId() == null ? null : history.getShopId().value(),
            history.getBaseOptionGroupId() == null ? null : history.getBaseOptionGroupId().value(),
            history.getMergedOptionGroupId() == null ? null : history.getMergedOptionGroupId().value(),
            history.getMergedGroupName(),
            history.getEntryType() == null ? null : history.getEntryType().name(),
            history.getActorCeoId() == null ? null : history.getActorCeoId().value()
        );
    }
}
