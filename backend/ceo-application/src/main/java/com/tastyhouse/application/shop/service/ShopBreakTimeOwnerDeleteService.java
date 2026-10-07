package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeOwnerDeleteUseCase;

@Service
@Transactional
class ShopBreakTimeOwnerDeleteService implements ShopBreakTimeOwnerDeleteUseCase {

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopBusinessHourOwnerValidator shopBusinessHourOwnerValidator;

    public ShopBreakTimeOwnerDeleteService(
        ShopBusinessHourService shopBusinessHourService,
        ShopBusinessHourOwnerValidator shopBusinessHourOwnerValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopBusinessHourOwnerValidator = shopBusinessHourOwnerValidator;
    }

    @Override
    public void deleteBreakTime(ShopBreakTimeOwnerDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long breakTimeId = command.breakTimeId();

        shopBusinessHourOwnerValidator.validateBreakTimeOwnership(ceoId, breakTimeId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.deleteBreakTime(breakTimeId, actor);
    }
}
