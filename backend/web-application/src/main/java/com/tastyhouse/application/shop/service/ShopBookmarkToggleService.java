package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.shop.port.in.ShopBookmarkToggleCommand;
import com.tastyhouse.application.shop.port.in.ShopBookmarkToggleUseCase;

@Service
@Transactional
class ShopBookmarkToggleService implements ShopBookmarkToggleUseCase {

    private final ShopLifecycleService shopLifecycleService;

    public ShopBookmarkToggleService(ShopLifecycleService shopLifecycleService) {
        this.shopLifecycleService = shopLifecycleService;
    }

    @Override
    public boolean toggleBookmark(ShopBookmarkToggleCommand command) {
        return shopLifecycleService.toggleBookmark(command.shopId(), MemberId.of(command.memberId()));
    }
}
