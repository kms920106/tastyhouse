package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopClosedDayOwnerDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopClosedDayOwnerDeleteUseCase;

@Service
@Transactional
class ShopClosedDayOwnerDeleteService implements ShopClosedDayOwnerDeleteUseCase {

    private final ShopBusinessHourService shopBusinessHourService;

    public ShopClosedDayOwnerDeleteService(ShopBusinessHourService shopBusinessHourService) {
        this.shopBusinessHourService = shopBusinessHourService;
    }

    @Override
    public void deleteClosedDay(ShopClosedDayOwnerDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long closedDayId = command.closedDayId();

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopBusinessHourService.deleteClosedDay(closedDayId, actor);
    }
}
