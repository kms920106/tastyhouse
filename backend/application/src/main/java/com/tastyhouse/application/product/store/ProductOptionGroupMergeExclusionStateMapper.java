package com.tastyhouse.application.product.store;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionState;

final class ProductOptionGroupMergeExclusionStateMapper {
    private ProductOptionGroupMergeExclusionStateMapper() {
    }

    static ProductOptionGroupMergeExclusion toDomain(ProductOptionGroupMergeExclusionState state) {
        return ProductOptionGroupMergeExclusion.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            state.groupSignature(),
            state.actorCeoId() == null ? null : CeoId.of(state.actorCeoId())
        );
    }

    static ProductOptionGroupMergeExclusionState toState(ProductOptionGroupMergeExclusion exclusion) {
        return new ProductOptionGroupMergeExclusionState(
            exclusion.getId(),
            exclusion.getShopId() == null ? null : exclusion.getShopId().value(),
            exclusion.getGroupSignature(),
            exclusion.getActorCeoId() == null ? null : exclusion.getActorCeoId().value()
        );
    }
}
