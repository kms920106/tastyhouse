package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductCategoryRepository {
    Optional<ProductCategory> findById(ProductCategoryId id);

    List<ProductCategory> findCategoriesByNameAndShopId(String name, ShopId shopId);

    ProductCategory save(ProductCategory productCategory);

    List<ProductCategory> findAllByShopId(ShopId shopId);

    void delete(ProductCategory productCategory);
}
