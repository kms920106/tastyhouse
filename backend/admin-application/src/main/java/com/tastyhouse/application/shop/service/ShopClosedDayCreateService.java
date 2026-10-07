package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.application.shop.port.in.ShopClosedDayCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopClosedDayManagementCreateCommand;

@Service
@Transactional
class ShopClosedDayCreateService implements ShopClosedDayCreateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;

    public ShopClosedDayCreateService(ShopBusinessHourService shopBusinessHourService) {
        this.shopBusinessHourService = shopBusinessHourService;
    }

    @Override
    public Long createClosedDay(ShopClosedDayManagementCreateCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        String closedDayType = command.closedDayType();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopClosedDay closedDay = shopBusinessHourService.createClosedDay(id, ClosedDayType.from(closedDayType), actor);
        return closedDay.getId();
    }
}
