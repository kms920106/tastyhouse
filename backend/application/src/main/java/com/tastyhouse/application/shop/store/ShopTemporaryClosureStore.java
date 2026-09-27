package com.tastyhouse.application.shop.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureStatePort;
import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;

public class ShopTemporaryClosureStore implements ShopTemporaryClosureRepository {
    private final ShopTemporaryClosureStatePort shopTemporaryClosureStatePort;

    public ShopTemporaryClosureStore(ShopTemporaryClosureStatePort shopTemporaryClosureStatePort) {
        this.shopTemporaryClosureStatePort = shopTemporaryClosureStatePort;
    }

    @Override
    public ShopTemporaryClosure save(ShopTemporaryClosure shopTemporaryClosure) {
        return ShopTemporaryClosureStateMapper.toDomain(shopTemporaryClosureStatePort.save(ShopTemporaryClosureStateMapper.toState(shopTemporaryClosure)));
    }

    @Override
    public List<ShopTemporaryClosure> findByShopId(Long shopId) {
        return shopTemporaryClosureStatePort.findByShopId(shopId).stream()
            .map(ShopTemporaryClosureStateMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopTemporaryClosure> findById(Long id) {
        return shopTemporaryClosureStatePort.findById(id).map(ShopTemporaryClosureStateMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopTemporaryClosureStatePort.deleteById(id);
    }
}
