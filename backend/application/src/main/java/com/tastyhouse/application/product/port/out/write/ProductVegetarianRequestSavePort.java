package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;

public interface ProductVegetarianRequestSavePort {

    ProductVegetarianRequest save(ProductVegetarianRequest request);
}
