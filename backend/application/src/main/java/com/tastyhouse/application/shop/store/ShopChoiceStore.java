package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.application.shop.port.out.write.ShopChoiceStatePort;

public class ShopChoiceStore implements ShopChoiceRepository {
    private final ShopChoiceStatePort shopChoiceStatePort;

    public ShopChoiceStore(ShopChoiceStatePort shopChoiceStatePort) {
        this.shopChoiceStatePort = shopChoiceStatePort;
    }

    @Override
    public Optional<ShopChoice> findById(Long id) {
        return shopChoiceStatePort.findById(id).map(ShopChoiceStateMapper::toDomain);
    }

    @Override
    public ShopChoice save(ShopChoice shopChoice) {
        return ShopChoiceStateMapper.toDomain(shopChoiceStatePort.save(ShopChoiceStateMapper.toState(shopChoice)));
    }

    @Override
    public void deleteById(Long id) {
        shopChoiceStatePort.deleteById(id);
    }
}
