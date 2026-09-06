package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class ShopOrderNoticeQueryService implements ShopOrderNoticeQueryUseCase {

    private final ShopOrderNoticeQueryPort shopOrderNoticeQueryPort;

    public ShopOrderNoticeQueryService(ShopOrderNoticeQueryPort shopOrderNoticeQueryPort) {
        this.shopOrderNoticeQueryPort = shopOrderNoticeQueryPort;
    }

    @Override
    public ShopOrderNoticeResult getOrderNotice(Long shopId) {
        return shopOrderNoticeQueryPort.findVisibleOrderNotice(shopId).orElse(null);
    }
}
