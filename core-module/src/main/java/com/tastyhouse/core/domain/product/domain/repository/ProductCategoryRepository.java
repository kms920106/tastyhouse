package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository {

    Optional<ProductCategory> findById(Long id);

    List<ProductCategory> findActiveCategoriesByPlaceIdOrderBySort(Long placeId);

    List<ProductCategory> findCategoriesByNameAndPlaceId(String name, Long placeId);

    ProductCategory save(ProductCategory productCategory);
}
