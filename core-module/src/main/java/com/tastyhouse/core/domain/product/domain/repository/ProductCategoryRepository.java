package com.tastyhouse.core.domain.product.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.product.domain.model.ProductCategory;

public interface ProductCategoryRepository {

    Optional<ProductCategory> findById(Long id);

    List<ProductCategory> findActiveCategoriesByShopIdOrderBySort(Long shopId);

    List<ProductCategory> findCategoriesByNameAndShopId(String name, Long shopId);

    ProductCategory save(ProductCategory productCategory);
}
