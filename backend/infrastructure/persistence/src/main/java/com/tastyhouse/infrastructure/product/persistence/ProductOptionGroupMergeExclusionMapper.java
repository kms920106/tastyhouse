package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ProductOptionGroupMergeExclusionMapper {

    private ProductOptionGroupMergeExclusionMapper() {
    }

    static ProductOptionGroupMergeExclusion toDomain(ProductOptionGroupMergeExclusionJpaEntity entity) {
        return ProductOptionGroupMergeExclusion.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            entity.getGroupSignature(),
            entity.getActorCeoId() == null ? null : CeoId.of(entity.getActorCeoId())
        );
    }

    static ProductOptionGroupMergeExclusionJpaEntity toEntity(ProductOptionGroupMergeExclusion exclusion) {
        return ProductOptionGroupMergeExclusionJpaEntity.create(
            exclusion.getShopId() == null ? null : exclusion.getShopId().value(),
            exclusion.getGroupSignature(),
            exclusion.getActorCeoId() == null ? null : exclusion.getActorCeoId().value()
        );
    }
}
