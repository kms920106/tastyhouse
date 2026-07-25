package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopConvenienceInfo;

public interface ShopConvenienceInfoRepository {

    Optional<ShopConvenienceInfo> findByShopId(Long shopId);

    ShopConvenienceInfo save(ShopConvenienceInfo shopConvenienceInfo);
}
