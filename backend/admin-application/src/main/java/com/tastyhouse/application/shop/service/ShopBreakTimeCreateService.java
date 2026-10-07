package com.tastyhouse.application.shop.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementCreateCommand;

@Service
@Transactional
class ShopBreakTimeCreateService implements ShopBreakTimeCreateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;

    public ShopBreakTimeCreateService(ShopBusinessHourService shopBusinessHourService) {
        this.shopBusinessHourService = shopBusinessHourService;
    }

    @Override
    public Long createBreakTime(ShopBreakTimeManagementCreateCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        String dayType = command.dayType();
        LocalTime startTime = command.startTime();
        LocalTime endTime = command.endTime();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopBreakTime breakTime = shopBusinessHourService.createBreakTime(
            id, DayType.from(dayType), startTime, endTime, actor
        );
        return breakTime.getId();
    }
}
