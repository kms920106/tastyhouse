package com.tastyhouse.application.shop.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerCreateUseCase;

@Service
@Transactional
class ShopBreakTimeOwnerCreateService implements ShopBreakTimeOwnerCreateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopBreakTimeOwnerCreateService(
        ShopBusinessHourService shopBusinessHourService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long createBreakTime(ShopBreakTimeOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String dayType = command.dayType();
        LocalTime startTime = command.startTime();
        LocalTime endTime = command.endTime();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        ShopBreakTime breakTime = shopBusinessHourService.createBreakTime(
            shopId, DayType.from(dayType), startTime, endTime, actor
        );
        return breakTime.getId();
    }
}
