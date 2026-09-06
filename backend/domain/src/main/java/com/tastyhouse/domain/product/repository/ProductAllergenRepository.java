package com.tastyhouse.domain.product.repository;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductAllergenRepository {
    List<ProductAllergen> findAllByProductId(ProductId productId);

    List<ProductAllergen> saveAll(List<ProductAllergen> productAllergens);

    void deleteAllByProductId(ProductId productId);
}
