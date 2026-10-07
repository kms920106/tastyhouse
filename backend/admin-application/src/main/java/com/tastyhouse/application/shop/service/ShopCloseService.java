package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopCloseCommand;
import com.tastyhouse.application.shop.port.in.ShopCloseUseCase;

@Service
@Transactional
class ShopCloseService implements ShopCloseUseCase {

    private final ShopLifecycleService shopLifecycleService;

    public ShopCloseService(ShopLifecycleService shopLifecycleService) {
        this.shopLifecycleService = shopLifecycleService;
    }

    @Override
    public void closeShop(ShopCloseCommand command) {
        Long id = command.shopId();

        ShopId shopId = ShopId.of(id);
        shopLifecycleService.closeShop(shopId);
    }
}
