package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopDetailViewResult;

public interface ShopDetailQueryUseCase {

    ShopDetailViewResult getShopDetail(Long shopId);
}
