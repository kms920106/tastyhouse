package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;

public interface ShopConvenienceInfoPersistencePort {

    Optional<ShopConvenienceInfo> findByShopId(Long shopId);

    ShopConvenienceInfo save(ShopConvenienceInfo shopConvenienceInfo);
}
