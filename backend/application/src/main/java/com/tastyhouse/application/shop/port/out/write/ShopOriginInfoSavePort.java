package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopOriginInfo;

public interface ShopOriginInfoSavePort {

    ShopOriginInfo save(ShopOriginInfo shopOriginInfo);
}
