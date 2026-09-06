package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;

@WebApp
public interface ShopOriginInfoQueryUseCase {

    ShopOriginInfoResult getOriginInfo(Long shopId);
}
