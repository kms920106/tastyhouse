package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeHideCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeHideUseCase;

@Service
@Transactional
class ShopOrderNoticeHideService implements ShopOrderNoticeHideUseCase {

    private final ShopOrderNoticeService shopOrderNoticeService;

    public ShopOrderNoticeHideService(ShopOrderNoticeService shopOrderNoticeService) {
        this.shopOrderNoticeService = shopOrderNoticeService;
    }

    @Override
    public void hideOrderNotice(ShopOrderNoticeHideCommand command) {
        Long shopId = command.shopId();
        String reason = command.reason();
        shopOrderNoticeService.hide(ShopId.of(shopId), reason);
    }
}
