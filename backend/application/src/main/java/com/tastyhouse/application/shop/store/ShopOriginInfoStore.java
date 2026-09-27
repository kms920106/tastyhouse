package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoStatePort;
import com.tastyhouse.domain.shop.model.ShopOriginInfo;

public class ShopOriginInfoStore implements ShopOriginInfoRepository {
    private final ShopOriginInfoStatePort shopOriginInfoStatePort;

    public ShopOriginInfoStore(ShopOriginInfoStatePort shopOriginInfoStatePort) {
        this.shopOriginInfoStatePort = shopOriginInfoStatePort;
    }

    @Override
    public Optional<ShopOriginInfo> findByShopId(Long shopId) {
        return shopOriginInfoStatePort.findByShopId(shopId).map(ShopOriginInfoStateMapper::toDomain);
    }

    @Override
    public ShopOriginInfo save(ShopOriginInfo shopOriginInfo) {
        return ShopOriginInfoStateMapper.toDomain(shopOriginInfoStatePort.save(ShopOriginInfoStateMapper.toState(shopOriginInfo)));
    }
}
