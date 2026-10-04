package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.shop.port.in.ShopBookmarkToggleCommand;
import com.tastyhouse.application.shop.port.in.ShopCommandUseCase;

@Service
@Transactional
public class ShopCommandService implements ShopCommandUseCase {

    private final ShopLifecycleService shopLifecycleService;

    public ShopCommandService(ShopLifecycleService shopLifecycleService) {
        this.shopLifecycleService = shopLifecycleService;
    }

    @Override
    public boolean toggleBookmark(ShopBookmarkToggleCommand command) {
        return shopLifecycleService.toggleBookmark(command.shopId(), MemberId.of(command.memberId()));
    }
}
