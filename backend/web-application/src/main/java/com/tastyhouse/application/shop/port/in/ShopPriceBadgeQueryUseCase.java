package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopPriceBadgeViewResult;

public interface ShopPriceBadgeQueryUseCase {

    ShopPriceBadgeViewResult getPriceBadges(Long shopId);
}
