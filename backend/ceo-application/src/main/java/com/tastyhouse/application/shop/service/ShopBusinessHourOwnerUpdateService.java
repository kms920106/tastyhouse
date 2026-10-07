package com.tastyhouse.application.shop.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerUpdateUseCase;

@Service
@Transactional
class ShopBusinessHourOwnerUpdateService implements ShopBusinessHourOwnerUpdateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopBusinessHourOwnerValidator shopBusinessHourOwnerValidator;

    public ShopBusinessHourOwnerUpdateService(
        ShopBusinessHourService shopBusinessHourService,
        ShopBusinessHourOwnerValidator shopBusinessHourOwnerValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopBusinessHourOwnerValidator = shopBusinessHourOwnerValidator;
    }

    @Override
    public void updateBusinessHour(ShopBusinessHourOwnerUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long businessHourId = command.businessHourId();
        String dayType = command.dayType();
        LocalTime openTime = command.openTime();
        LocalTime closeTime = command.closeTime();
        Boolean isClosed = command.isClosed();
        Boolean is24Hours = command.is24Hours();

        shopBusinessHourOwnerValidator.validateBusinessHourOwnership(ceoId, businessHourId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.updateBusinessHour(
            businessHourId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
    }
}
