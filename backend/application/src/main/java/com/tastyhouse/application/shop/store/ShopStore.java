package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopStatePort;

public class ShopStore implements ShopRepository {
    private final ShopStatePort shopStatePort;

    public ShopStore(ShopStatePort shopStatePort) {
        this.shopStatePort = shopStatePort;
    }

    @Override
    public Optional<Shop> findById(ShopId id) {
        return shopStatePort.findById(id.value()).map(ShopStateMapper::toDomain);
    }

    @Override
    public Optional<Shop> findVisibleById(ShopId id) {
        return shopStatePort.findVisibleById(id.value()).map(ShopStateMapper::toDomain);
    }

    @Override
    public Shop save(Shop shop) {
        return ShopStateMapper.toDomain(shopStatePort.save(ShopStateMapper.toState(shop)));
    }
}
