package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionState;

final class ProductOptionGroupMergeExclusionMapper {
    private ProductOptionGroupMergeExclusionMapper() {
    }

    static ProductOptionGroupMergeExclusionState toState(ProductOptionGroupMergeExclusionJpaEntity entity) {
        return new ProductOptionGroupMergeExclusionState(
            entity.getId(),
            entity.getShopId(),
            entity.getGroupSignature(),
            entity.getActorCeoId()
        );
    }

    static ProductOptionGroupMergeExclusionJpaEntity toEntity(ProductOptionGroupMergeExclusionState state) {
        return ProductOptionGroupMergeExclusionJpaEntity.create(
            state.shopId(),
            state.groupSignature(),
            state.actorCeoId()
        );
    }
}
