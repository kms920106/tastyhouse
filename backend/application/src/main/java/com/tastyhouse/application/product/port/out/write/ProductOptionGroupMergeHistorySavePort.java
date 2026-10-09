package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;

public interface ProductOptionGroupMergeHistorySavePort {

    ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history);
}
