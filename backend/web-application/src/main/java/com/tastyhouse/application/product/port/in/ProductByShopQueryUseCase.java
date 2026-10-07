package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ShopProductItemResult;

public interface ProductByShopQueryUseCase {

    List<ShopProductItemResult> findShopProducts(Long shopId);
}
