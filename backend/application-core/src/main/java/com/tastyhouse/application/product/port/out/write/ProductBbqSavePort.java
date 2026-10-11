package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductBbq;

public interface ProductBbqSavePort {

    ProductBbq save(ProductBbq productBbq);
}
