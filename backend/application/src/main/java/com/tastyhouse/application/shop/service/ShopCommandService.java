package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.service.ShopLifecycleService;
import com.tastyhouse.application.shop.port.in.ShopBookmarkToggleCommand;
import com.tastyhouse.application.shop.port.in.ShopCommandUseCase;

@Service
@WebApp
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
