package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ProductCategoryLoadPort {

    Optional<ProductCategory> findById(ProductCategoryId id);

    List<ProductCategory> findCategoriesByNameAndShopId(String name, ShopId shopId);

    List<ProductCategory> findAllByShopId(ShopId shopId);
}
