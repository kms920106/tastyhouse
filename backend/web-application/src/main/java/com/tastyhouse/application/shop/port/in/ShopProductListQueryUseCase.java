package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopProductCategoryViewResult;

public interface ShopProductListQueryUseCase {

    List<ShopProductCategoryViewResult> getShopProducts(Long shopId);
}
