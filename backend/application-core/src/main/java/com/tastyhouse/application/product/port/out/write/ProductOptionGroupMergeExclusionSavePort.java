package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;

public interface ProductOptionGroupMergeExclusionSavePort {

    ProductOptionGroupMergeExclusion save(ProductOptionGroupMergeExclusion exclusion);
}
