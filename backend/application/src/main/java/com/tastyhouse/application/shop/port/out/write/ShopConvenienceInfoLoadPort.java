package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;

public interface ShopConvenienceInfoLoadPort {

    Optional<ShopConvenienceInfo> findByShopId(Long shopId);
}
