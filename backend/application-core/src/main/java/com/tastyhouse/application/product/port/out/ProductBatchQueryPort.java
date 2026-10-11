package com.tastyhouse.application.product.port.out;

import java.util.List;

public interface ProductBatchQueryPort {

    List<ProductBatchResult> findProductsBatch(List<ProductBatchItem> items);
}
