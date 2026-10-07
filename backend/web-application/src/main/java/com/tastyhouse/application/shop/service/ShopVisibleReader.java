package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.application.shop.port.out.ShopVisibleDetailResult;

@Component
class ShopVisibleReader {

    private final ShopQueryPort shopQueryPort;

    public ShopVisibleReader(ShopQueryPort shopQueryPort) {
        this.shopQueryPort = shopQueryPort;
    }

    public ShopVisibleDetailResult findVisibleShop(Long shopId) {
        return shopQueryPort.findVisibleDetailById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
    }
}
