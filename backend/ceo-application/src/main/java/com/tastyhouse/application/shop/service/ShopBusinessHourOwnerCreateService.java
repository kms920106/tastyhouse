package com.tastyhouse.application.shop.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerCreateUseCase;

@Service
@Transactional
class ShopBusinessHourOwnerCreateService implements ShopBusinessHourOwnerCreateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopBusinessHourOwnerCreateService(
        ShopBusinessHourService shopBusinessHourService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long createBusinessHour(ShopBusinessHourOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String dayType = command.dayType();
        LocalTime openTime = command.openTime();
        LocalTime closeTime = command.closeTime();
        Boolean isClosed = command.isClosed();
        Boolean is24Hours = command.is24Hours();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        ShopBusinessHour businessHour = shopBusinessHourService.createBusinessHour(
            shopId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
        return businessHour.getId();
    }
}
