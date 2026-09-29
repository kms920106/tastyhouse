package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductOptionGroupMergeExclusionPersistencePort {
    ProductOptionGroupMergeExclusion save(ProductOptionGroupMergeExclusion exclusion);

    Optional<ProductOptionGroupMergeExclusion> findByShopIdAndGroupSignature(
        ShopId shopId,
        String groupSignature
    );

    List<ProductOptionGroupMergeExclusion> findAllByShopId(ShopId shopId);
}
