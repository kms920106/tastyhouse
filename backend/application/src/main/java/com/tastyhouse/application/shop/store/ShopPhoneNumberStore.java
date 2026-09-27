package com.tastyhouse.application.shop.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberStatePort;

public class ShopPhoneNumberStore implements ShopPhoneNumberRepository {
    private final ShopPhoneNumberStatePort shopPhoneNumberStatePort;

    public ShopPhoneNumberStore(ShopPhoneNumberStatePort shopPhoneNumberStatePort) {
        this.shopPhoneNumberStatePort = shopPhoneNumberStatePort;
    }

    @Override
    public ShopPhoneNumber save(ShopPhoneNumber shopPhoneNumber) {
        return ShopPhoneNumberStateMapper.toDomain(shopPhoneNumberStatePort.save(ShopPhoneNumberStateMapper.toState(shopPhoneNumber)));
    }

    @Override
    public List<ShopPhoneNumber> findByShopId(Long shopId) {
        return shopPhoneNumberStatePort.findByShopId(shopId).stream()
            .map(ShopPhoneNumberStateMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopPhoneNumber> findById(Long id) {
        return shopPhoneNumberStatePort.findById(id).map(ShopPhoneNumberStateMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopPhoneNumberStatePort.deleteById(id);
    }
}
