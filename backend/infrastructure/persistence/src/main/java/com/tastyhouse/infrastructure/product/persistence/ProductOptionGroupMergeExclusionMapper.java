package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductOptionGroupMergeExclusionMapper {
    private ProductOptionGroupMergeExclusionMapper() {
    }

    static ProductOptionGroupMergeExclusion toDomain(ProductOptionGroupMergeExclusionJpaEntity entity) {
        return ProductOptionGroupMergeExclusion.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getGroupSignature(),
            IdMapping.vo(entity.getActorCeoId(), CeoId::of)
        );
    }

    static ProductOptionGroupMergeExclusionJpaEntity toEntity(ProductOptionGroupMergeExclusion domain) {
        return ProductOptionGroupMergeExclusionJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getGroupSignature(),
            IdMapping.raw(domain.getActorCeoId(), CeoId::value)
        );
    }
}
