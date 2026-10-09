package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductCategory;

public interface ProductCategorySavePort {

    ProductCategory save(ProductCategory productCategory);

    void delete(ProductCategory productCategory);
}
