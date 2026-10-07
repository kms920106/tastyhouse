package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopNoticeQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopNoticeQueryPort;
import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

@Service
@Transactional(readOnly = true)
class ShopNoticeQueryService implements ShopNoticeQueryUseCase {

    private final ShopVisibleReader shopVisibleReader;
    private final ShopNoticeQueryPort shopNoticeQueryPort;

    public ShopNoticeQueryService(ShopVisibleReader shopVisibleReader, ShopNoticeQueryPort shopNoticeQueryPort) {
        this.shopVisibleReader = shopVisibleReader;
        this.shopNoticeQueryPort = shopNoticeQueryPort;
    }

    @Override
    public ShopNoticeResult getShopNotice(Long shopId) {
        shopVisibleReader.findVisibleShop(shopId);
        return shopNoticeQueryPort.findExposedNotice(shopId).orElse(null);
    }
}
