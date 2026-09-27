package com.tastyhouse.application.shop.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopSuspensionStatePort;
import com.tastyhouse.domain.shop.model.ShopSuspension;

public class ShopSuspensionStore implements ShopSuspensionRepository {
    private final ShopSuspensionStatePort shopSuspensionStatePort;

    public ShopSuspensionStore(ShopSuspensionStatePort shopSuspensionStatePort) {
        this.shopSuspensionStatePort = shopSuspensionStatePort;
    }

    @Override
    public ShopSuspension save(ShopSuspension shopSuspension) {
        return ShopSuspensionStateMapper.toDomain(shopSuspensionStatePort.save(ShopSuspensionStateMapper.toState(shopSuspension)));
    }

    @Override
    public List<ShopSuspension> findByShopId(Long shopId) {
        return shopSuspensionStatePort.findByShopId(shopId).stream()
            .map(ShopSuspensionStateMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopSuspension> findById(Long id) {
        return shopSuspensionStatePort.findById(id).map(ShopSuspensionStateMapper::toDomain);
    }
}
