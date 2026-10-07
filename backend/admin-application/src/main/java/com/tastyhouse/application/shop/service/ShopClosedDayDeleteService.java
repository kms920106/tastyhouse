package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopClosedDayDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopClosedDayManagementDeleteCommand;

@Service
@Transactional
class ShopClosedDayDeleteService implements ShopClosedDayDeleteUseCase {

    private final ShopBusinessHourService shopBusinessHourService;

    public ShopClosedDayDeleteService(ShopBusinessHourService shopBusinessHourService) {
        this.shopBusinessHourService = shopBusinessHourService;
    }

    @Override
    public void deleteClosedDay(ShopClosedDayManagementDeleteCommand command) {
        Long adminId = command.adminId();
        Long closedDayId = command.closedDayId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteClosedDay(closedDayId, actor);
    }
}
