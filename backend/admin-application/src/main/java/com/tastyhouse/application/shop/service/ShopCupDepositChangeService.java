package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopCupDepositChangeCommand;
import com.tastyhouse.application.shop.port.in.ShopCupDepositChangeUseCase;

@Service
@Transactional
class ShopCupDepositChangeService implements ShopCupDepositChangeUseCase {

    private final ShopLifecycleService shopLifecycleService;

    public ShopCupDepositChangeService(ShopLifecycleService shopLifecycleService) {
        this.shopLifecycleService = shopLifecycleService;
    }

    @Override
    public void changeCupDepositEnabled(ShopCupDepositChangeCommand command) {
        Long id = command.shopId();
        boolean enabled = command.enabled();

        ShopId shopId = ShopId.of(id);
        shopLifecycleService.changeCupDepositEnabled(shopId, enabled);
    }
}
