package com.tastyhouse.application.shop.service;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;

public class StorePriceVerificationAdapter implements StorePriceVerificationPort {

    private final ShopPersistencePort shopPersistencePort;

    public StorePriceVerificationAdapter(ShopPersistencePort shopPersistencePort) {
        this.shopPersistencePort = shopPersistencePort;
    }

    @Override
    public boolean isStorePriceVerified(Long shopId) {
        return loadShop(shopId).isStorePriceVerified();
    }

    @Override
    public void verifyStorePrice(Long shopId) {
        Shop shop = loadShop(shopId);
        shop.verifyStorePrice();
        shopPersistencePort.save(shop);
    }

    @Override
    public void clearStorePriceVerification(Long shopId) {
        Shop shop = loadShop(shopId);
        shop.clearStorePriceVerification();
        shopPersistencePort.save(shop);
    }

    private Shop loadShop(Long shopId) {
        return shopPersistencePort.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }
}
