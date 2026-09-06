package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shop.port.out.ShopPriceBadgeViewResult;

@WebApp
public interface ShopPriceBadgeQueryUseCase {

    ShopPriceBadgeViewResult getPriceBadges(Long shopId);
}
