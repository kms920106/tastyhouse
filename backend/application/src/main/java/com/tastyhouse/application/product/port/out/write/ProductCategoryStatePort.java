package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryStatePort {
    Optional<ProductCategoryState> findById(Long id);

    List<ProductCategoryState> findCategoriesByNameAndShopId(String name, Long shopId);

    ProductCategoryState save(ProductCategoryState productCategory);

    List<ProductCategoryState> findAllByShopId(Long shopId);

    void deleteById(Long id);
}
