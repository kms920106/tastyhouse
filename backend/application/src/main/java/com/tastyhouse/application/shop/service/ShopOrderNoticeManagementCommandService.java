package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopOrderNoticeManagementCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeHideCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeUnhideCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopOrderNoticeService;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@AdminApp
@Transactional
public class ShopOrderNoticeManagementCommandService implements ShopOrderNoticeManagementCommandUseCase {

    private final ShopOrderNoticeService shopOrderNoticeService;

    public ShopOrderNoticeManagementCommandService(ShopOrderNoticeService shopOrderNoticeService) {
        this.shopOrderNoticeService = shopOrderNoticeService;
    }

    @Override
    public void hideOrderNotice(ShopOrderNoticeHideCommand command) {
        Long shopId = command.shopId();
        String reason = command.reason();
        shopOrderNoticeService.hide(ShopId.of(shopId), reason);
    }

    @Override
    public void unhideOrderNotice(ShopOrderNoticeUnhideCommand command) {
        Long shopId = command.shopId();
        shopOrderNoticeService.unhide(ShopId.of(shopId));
    }
}
