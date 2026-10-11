package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopOriginInfo;

public interface ShopOriginInfoLoadPort {

    Optional<ShopOriginInfo> findByShopId(Long shopId);
}
