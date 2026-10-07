package com.tastyhouse.application.shop.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerUpdateUseCase;

@Service
@Transactional
class ShopBreakTimeOwnerUpdateService implements ShopBreakTimeOwnerUpdateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopBusinessHourOwnerValidator shopBusinessHourOwnerValidator;

    public ShopBreakTimeOwnerUpdateService(
        ShopBusinessHourService shopBusinessHourService,
        ShopBusinessHourOwnerValidator shopBusinessHourOwnerValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopBusinessHourOwnerValidator = shopBusinessHourOwnerValidator;
    }

    @Override
    public void updateBreakTime(ShopBreakTimeOwnerUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long breakTimeId = command.breakTimeId();
        String dayType = command.dayType();
        LocalTime startTime = command.startTime();
        LocalTime endTime = command.endTime();

        shopBusinessHourOwnerValidator.validateBreakTimeOwnership(ceoId, breakTimeId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.updateBreakTime(breakTimeId, DayType.from(dayType), startTime, endTime, actor);
    }
}
