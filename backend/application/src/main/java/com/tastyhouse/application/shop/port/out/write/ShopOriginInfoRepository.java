package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopOriginInfo;

public interface ShopOriginInfoRepository {

    Optional<ShopOriginInfo> findByShopId(Long shopId);

    ShopOriginInfo save(ShopOriginInfo shopOriginInfo);
}
