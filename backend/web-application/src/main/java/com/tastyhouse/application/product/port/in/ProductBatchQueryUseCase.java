package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductBatchItemView;

public interface ProductBatchQueryUseCase {

    List<ProductBatchItemView> findProductsBatch(ProductBatchQuery query);
}
