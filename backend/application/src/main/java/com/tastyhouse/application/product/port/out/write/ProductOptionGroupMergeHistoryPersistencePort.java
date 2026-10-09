package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeHistory;

public interface ProductOptionGroupMergeHistoryPersistencePort {

    ProductOptionGroupMergeHistory save(ProductOptionGroupMergeHistory history);
}
