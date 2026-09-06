package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;

public interface ShopConvenienceInfoRepository {

    Optional<ShopConvenienceInfo> findByShopId(Long shopId);

    ShopConvenienceInfo save(ShopConvenienceInfo shopConvenienceInfo);
}
