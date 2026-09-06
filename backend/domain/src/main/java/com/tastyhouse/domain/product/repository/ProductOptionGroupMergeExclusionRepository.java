package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductOptionGroupMergeExclusionRepository {
    ProductOptionGroupMergeExclusion save(ProductOptionGroupMergeExclusion exclusion);

    Optional<ProductOptionGroupMergeExclusion> findByShopIdAndGroupSignature(
        ShopId shopId,
        String groupSignature
    );

    List<ProductOptionGroupMergeExclusion> findAllByShopId(ShopId shopId);
}
