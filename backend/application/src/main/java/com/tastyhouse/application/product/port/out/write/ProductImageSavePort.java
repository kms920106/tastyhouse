package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductImage;

public interface ProductImageSavePort {

    ProductImage save(ProductImage productImage);

    void delete(ProductImage productImage);
}
