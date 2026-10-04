package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;

public interface ShopOriginInfoQueryUseCase {

    ShopOriginInfoResult getOriginInfo(Long shopId);
}
