package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.PopularProductItemResult;

public interface ShopPopularProductQueryUseCase {

    List<PopularProductItemResult> getPopularProducts(Long shopId);
}
