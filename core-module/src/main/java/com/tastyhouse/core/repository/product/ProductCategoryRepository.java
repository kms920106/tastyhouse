package com.tastyhouse.core.repository.product;

import com.tastyhouse.core.entity.product.ProductCategory;

import java.util.List;

public interface ProductCategoryRepository {

    List<ProductCategory> findByNameAndPlaceId(String name, Long placeId);
}
