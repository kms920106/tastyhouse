package com.tastyhouse.application.shop.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeUpdateUseCase;

@Service
@Transactional
class ShopBreakTimeUpdateService implements ShopBreakTimeUpdateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;

    public ShopBreakTimeUpdateService(ShopBusinessHourService shopBusinessHourService) {
        this.shopBusinessHourService = shopBusinessHourService;
    }

    @Override
    public void updateBreakTime(ShopBreakTimeManagementUpdateCommand command) {
        Long adminId = command.adminId();
        Long breakTimeId = command.breakTimeId();
        String dayType = command.dayType();
        LocalTime startTime = command.startTime();
        LocalTime endTime = command.endTime();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.updateBreakTime(breakTimeId, DayType.from(dayType), startTime, endTime, actor);
    }
}
