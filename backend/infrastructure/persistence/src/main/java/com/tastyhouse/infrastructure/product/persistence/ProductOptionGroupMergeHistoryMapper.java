package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryState;

final class ProductOptionGroupMergeHistoryMapper {
    private ProductOptionGroupMergeHistoryMapper() {
    }

    static ProductOptionGroupMergeHistoryState toState(ProductOptionGroupMergeHistoryJpaEntity entity) {
        return new ProductOptionGroupMergeHistoryState(
            entity.getId(),
            entity.getShopId(),
            entity.getBaseOptionGroupId(),
            entity.getMergedOptionGroupId(),
            entity.getMergedGroupName(),
            entity.getEntryType(),
            entity.getActorCeoId()
        );
    }

    static ProductOptionGroupMergeHistoryJpaEntity toEntity(ProductOptionGroupMergeHistoryState state) {
        return ProductOptionGroupMergeHistoryJpaEntity.create(
            state.shopId(),
            state.baseOptionGroupId(),
            state.mergedOptionGroupId(),
            state.mergedGroupName(),
            state.entryType(),
            state.actorCeoId()
        );
    }
}
