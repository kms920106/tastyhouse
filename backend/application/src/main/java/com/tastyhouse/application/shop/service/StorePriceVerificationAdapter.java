package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.shop.store.ShopRepository;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;

public class StorePriceVerificationAdapter implements StorePriceVerificationPort {
    private final ShopRepository shopRepository;

    public StorePriceVerificationAdapter(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Override
    public boolean isStorePriceVerified(Long shopId) {
        return loadShop(shopId).isStorePriceVerified();
    }

    @Override
    public void verifyStorePrice(Long shopId) {
        Shop shop = loadShop(shopId);
        shop.verifyStorePrice();
        shopRepository.save(shop);
    }

    @Override
    public void clearStorePriceVerification(Long shopId) {
        Shop shop = loadShop(shopId);
        shop.clearStorePriceVerification();
        shopRepository.save(shop);
    }

    private Shop loadShop(Long shopId) {
        return shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }
}
