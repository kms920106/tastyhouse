package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductImageChangeRequest;

public interface ProductImageChangeRequestSavePort {

    ProductImageChangeRequest save(ProductImageChangeRequest request);
}
