package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.Product;

public interface ProductSavePort {

    Product save(Product product);
}
