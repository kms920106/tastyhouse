package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ShopOrderNoticeManagementQueryService implements ShopOrderNoticeManagementQueryUseCase {

    private final ShopOrderNoticeManagementQueryPort shopOrderNoticeManagementQueryPort;

    public ShopOrderNoticeManagementQueryService(ShopOrderNoticeManagementQueryPort shopOrderNoticeManagementQueryPort) {
        this.shopOrderNoticeManagementQueryPort = shopOrderNoticeManagementQueryPort;
    }

    @Override
    public Optional<ShopOrderNoticeResult> getOrderNotice(Long shopId) {
        return shopOrderNoticeManagementQueryPort.findOrderNotice(shopId);
    }
}
