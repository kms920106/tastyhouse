package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository {

    Optional<ProductCategory> findById(Long id);

    List<ProductCategory> findActiveCategoriesByShopIdOrderBySort(Long shopId);

    List<ProductCategory> findCategoriesByNameAndShopId(String name, Long shopId);

    ProductCategory save(ProductCategory productCategory);
}
