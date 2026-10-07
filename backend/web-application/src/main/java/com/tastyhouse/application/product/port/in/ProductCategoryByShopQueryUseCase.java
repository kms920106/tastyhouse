package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductCategoryResult;

public interface ProductCategoryByShopQueryUseCase {

    List<ProductCategoryResult> findShopProductCategories(Long shopId);
}
