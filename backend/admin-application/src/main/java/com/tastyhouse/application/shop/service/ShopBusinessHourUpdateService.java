package com.tastyhouse.application.shop.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourUpdateUseCase;

@Service
@Transactional
class ShopBusinessHourUpdateService implements ShopBusinessHourUpdateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;

    public ShopBusinessHourUpdateService(ShopBusinessHourService shopBusinessHourService) {
        this.shopBusinessHourService = shopBusinessHourService;
    }

    @Override
    public void updateBusinessHour(ShopBusinessHourManagementUpdateCommand command) {
        Long adminId = command.adminId();
        Long businessHourId = command.businessHourId();
        String dayType = command.dayType();
        LocalTime openTime = command.openTime();
        LocalTime closeTime = command.closeTime();
        Boolean isClosed = command.isClosed();
        Boolean is24Hours = command.is24Hours();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.updateBusinessHour(
            businessHourId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
    }
}
