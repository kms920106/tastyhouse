package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductOptionGroupMergeExclusionStatePort {
    ProductOptionGroupMergeExclusionState save(ProductOptionGroupMergeExclusionState exclusion);

    Optional<ProductOptionGroupMergeExclusionState> findByShopIdAndGroupSignature(Long shopId, String groupSignature);

    List<ProductOptionGroupMergeExclusionState> findAllByShopId(Long shopId);
}
