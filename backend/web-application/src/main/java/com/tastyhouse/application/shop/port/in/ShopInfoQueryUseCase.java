package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopInfoViewResult;

public interface ShopInfoQueryUseCase {

    ShopInfoViewResult getShopInfo(Long shopId);
}
