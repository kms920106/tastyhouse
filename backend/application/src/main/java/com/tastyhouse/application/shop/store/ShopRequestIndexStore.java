package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexStatePort;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestType;

public class ShopRequestIndexStore implements ShopRequestIndexRepository {
    private final ShopRequestIndexStatePort shopRequestIndexStatePort;

    public ShopRequestIndexStore(ShopRequestIndexStatePort shopRequestIndexStatePort) {
        this.shopRequestIndexStatePort = shopRequestIndexStatePort;
    }

    @Override
    public ShopRequestIndex save(ShopRequestIndex shopRequestIndex) {
        return ShopRequestIndexStateMapper.toDomain(shopRequestIndexStatePort.save(ShopRequestIndexStateMapper.toState(shopRequestIndex)));
    }

    @Override
    public Optional<ShopRequestIndex> findById(Long id) {
        return shopRequestIndexStatePort.findById(id).map(ShopRequestIndexStateMapper::toDomain);
    }

    @Override
    public Optional<ShopRequestIndex> findByRequestTypeAndSourceRequestId(ShopRequestType requestType, Long sourceRequestId) {
        return shopRequestIndexStatePort.findByRequestTypeAndSourceRequestId(
            requestType == null ? null : requestType.name(),
            sourceRequestId
        ).map(ShopRequestIndexStateMapper::toDomain);
    }
}
