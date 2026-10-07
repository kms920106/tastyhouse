package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.application.shop.port.in.ShopClosedDayOwnerCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopClosedDayOwnerCreateUseCase;

@Service
@Transactional
class ShopClosedDayOwnerCreateService implements ShopClosedDayOwnerCreateUseCase {

    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopClosedDayOwnerCreateService(
        ShopBusinessHourService shopBusinessHourService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long createClosedDay(ShopClosedDayOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String closedDayType = command.closedDayType();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        ShopClosedDay closedDay = shopBusinessHourService.createClosedDay(
            shopId, ClosedDayType.from(closedDayType), actor
        );
        return closedDay.getId();
    }
}
