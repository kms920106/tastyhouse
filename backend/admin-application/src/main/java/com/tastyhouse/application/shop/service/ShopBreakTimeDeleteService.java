package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementDeleteCommand;

@Service
@Transactional
class ShopBreakTimeDeleteService implements ShopBreakTimeDeleteUseCase {

    private final ShopBusinessHourService shopBusinessHourService;

    public ShopBreakTimeDeleteService(ShopBusinessHourService shopBusinessHourService) {
        this.shopBusinessHourService = shopBusinessHourService;
    }

    @Override
    public void deleteBreakTime(ShopBreakTimeManagementDeleteCommand command) {
        Long adminId = command.adminId();
        Long breakTimeId = command.breakTimeId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteBreakTime(breakTimeId, actor);
    }
}
