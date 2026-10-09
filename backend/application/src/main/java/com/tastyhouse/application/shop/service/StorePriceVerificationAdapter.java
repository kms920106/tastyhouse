package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.StorePriceVerificationPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopSavePort;

@Service
public class StorePriceVerificationAdapter implements StorePriceVerificationPort {

    private final ShopLoadPort shopLoadPort;
    private final ShopSavePort shopSavePort;

    public StorePriceVerificationAdapter(ShopLoadPort shopLoadPort, ShopSavePort shopSavePort) {
        this.shopLoadPort = shopLoadPort;
        this.shopSavePort = shopSavePort;
    }

    @Override
    public boolean isStorePriceVerified(Long shopId) {
        return loadShop(shopId).isStorePriceVerified();
    }

    @Override
    public void verifyStorePrice(Long shopId) {
        Shop shop = loadShop(shopId);
        shop.verifyStorePrice();
        shopSavePort.save(shop);
    }

    @Override
    public void clearStorePriceVerification(Long shopId) {
        Shop shop = loadShop(shopId);
        shop.clearStorePriceVerification();
        shopSavePort.save(shop);
    }

    private Shop loadShop(Long shopId) {
        return shopLoadPort.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
    }
}
