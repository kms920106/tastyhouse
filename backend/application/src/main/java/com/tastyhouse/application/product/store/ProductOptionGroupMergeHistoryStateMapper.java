package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeHistoryState;

final class ProductOptionGroupMergeHistoryStateMapper {
    private ProductOptionGroupMergeHistoryStateMapper() {
    }

    static ProductOptionGroupMergeHistory toDomain(ProductOptionGroupMergeHistoryState state) {
        return ProductOptionGroupMergeHistory.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.baseOptionGroupId() == null ? null : ProductOptionGroupId.of(state.baseOptionGroupId()),
            state.mergedOptionGroupId() == null ? null : ProductOptionGroupId.of(state.mergedOptionGroupId()),
            state.mergedGroupName(),
            state.entryType() == null ? null : ProductOptionGroupMergeEntryType.valueOf(state.entryType()),
            state.actorCeoId() == null ? null : CeoId.of(state.actorCeoId())
        );
    }

    static ProductOptionGroupMergeHistoryState toState(ProductOptionGroupMergeHistory history) {
        return new ProductOptionGroupMergeHistoryState(
            history.getId(),
            history.getShopId() == null ? null : history.getShopId().value(),
            history.getBaseOptionGroupId() == null ? null : history.getBaseOptionGroupId().value(),
            history.getMergedOptionGroupId() == null ? null : history.getMergedOptionGroupId().value(),
            history.getMergedGroupName(),
            history.getEntryType() == null ? null : history.getEntryType().name(),
            history.getActorCeoId() == null ? null : history.getActorCeoId().value()
        );
    }
}
