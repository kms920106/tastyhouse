package com.tastyhouse.application.shop.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementCreateCommand;

@Service
@Transactional
class ShopBusinessHourCreateService implements ShopBusinessHourCreateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;

    public ShopBusinessHourCreateService(ShopBusinessHourService shopBusinessHourService) {
        this.shopBusinessHourService = shopBusinessHourService;
    }

    @Override
    public Long createBusinessHour(ShopBusinessHourManagementCreateCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        String dayType = command.dayType();
        LocalTime openTime = command.openTime();
        LocalTime closeTime = command.closeTime();
        Boolean isClosed = command.isClosed();
        Boolean is24Hours = command.is24Hours();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopBusinessHour businessHour = shopBusinessHourService.createBusinessHour(
            id, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
        return businessHour.getId();
    }
}
