package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourOwnerDeleteUseCase;

@Service
@Transactional
class ShopBusinessHourOwnerDeleteService implements ShopBusinessHourOwnerDeleteUseCase {

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopBusinessHourOwnerValidator shopBusinessHourOwnerValidator;

    public ShopBusinessHourOwnerDeleteService(
        ShopBusinessHourService shopBusinessHourService,
        ShopBusinessHourOwnerValidator shopBusinessHourOwnerValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopBusinessHourOwnerValidator = shopBusinessHourOwnerValidator;
    }

    @Override
    public void deleteBusinessHour(ShopBusinessHourOwnerDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long businessHourId = command.businessHourId();

        shopBusinessHourOwnerValidator.validateBusinessHourOwnership(ceoId, businessHourId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.deleteBusinessHour(businessHourId, actor);
    }
}
