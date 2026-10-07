package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeUnhideCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeUnhideUseCase;

@Service
@Transactional
class ShopOrderNoticeUnhideService implements ShopOrderNoticeUnhideUseCase {

    private final ShopOrderNoticeService shopOrderNoticeService;

    public ShopOrderNoticeUnhideService(ShopOrderNoticeService shopOrderNoticeService) {
        this.shopOrderNoticeService = shopOrderNoticeService;
    }

    @Override
    public void unhideOrderNotice(ShopOrderNoticeUnhideCommand command) {
        Long shopId = command.shopId();
        shopOrderNoticeService.unhide(ShopId.of(shopId));
    }
}
