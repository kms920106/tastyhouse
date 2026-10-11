package com.tastyhouse.application.product.port.out.write;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductAllergenSavePort {

    List<ProductAllergen> saveAll(List<ProductAllergen> productAllergens);

    void deleteAllByProductId(ProductId productId);
}
