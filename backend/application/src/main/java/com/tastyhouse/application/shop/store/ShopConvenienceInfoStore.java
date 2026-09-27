package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoStatePort;
import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;

public class ShopConvenienceInfoStore implements ShopConvenienceInfoRepository {
    private final ShopConvenienceInfoStatePort shopConvenienceInfoStatePort;

    public ShopConvenienceInfoStore(ShopConvenienceInfoStatePort shopConvenienceInfoStatePort) {
        this.shopConvenienceInfoStatePort = shopConvenienceInfoStatePort;
    }

    @Override
    public Optional<ShopConvenienceInfo> findByShopId(Long shopId) {
        return shopConvenienceInfoStatePort.findByShopId(shopId).map(ShopConvenienceInfoStateMapper::toDomain);
    }

    @Override
    public ShopConvenienceInfo save(ShopConvenienceInfo shopConvenienceInfo) {
        return ShopConvenienceInfoStateMapper.toDomain(shopConvenienceInfoStatePort.save(ShopConvenienceInfoStateMapper.toState(shopConvenienceInfo)));
    }
}
