package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgeStatePort;
import com.tastyhouse.domain.shop.model.ShopHygieneBadge;

public class ShopHygieneBadgeStore implements ShopHygieneBadgeRepository {
    private final ShopHygieneBadgeStatePort shopHygieneBadgeStatePort;

    public ShopHygieneBadgeStore(ShopHygieneBadgeStatePort shopHygieneBadgeStatePort) {
        this.shopHygieneBadgeStatePort = shopHygieneBadgeStatePort;
    }

    @Override
    public Optional<ShopHygieneBadge> findById(Long id) {
        return shopHygieneBadgeStatePort.findById(id).map(ShopHygieneBadgeStateMapper::toDomain);
    }

    @Override
    public ShopHygieneBadge save(ShopHygieneBadge shopHygieneBadge) {
        return ShopHygieneBadgeStateMapper.toDomain(shopHygieneBadgeStatePort.save(ShopHygieneBadgeStateMapper.toState(shopHygieneBadge)));
    }

    @Override
    public void deleteById(Long id) {
        shopHygieneBadgeStatePort.deleteById(id);
    }
}
