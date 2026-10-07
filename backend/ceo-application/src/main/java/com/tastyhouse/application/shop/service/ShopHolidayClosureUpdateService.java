package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopHolidayClosureUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopHolidayClosureUpdateUseCase;

@Service
@Transactional
class ShopHolidayClosureUpdateService implements ShopHolidayClosureUpdateUseCase {

    private final ShopLifecycleService shopLifecycleService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopHolidayClosureUpdateService(
        ShopLifecycleService shopLifecycleService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopLifecycleService = shopLifecycleService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateHolidayClosure(ShopHolidayClosureUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        boolean closedOnPublicHolidays = command.closedOnPublicHolidays();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopId targetShopId = ShopId.of(shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopLifecycleService.updateHolidayClosure(targetShopId, closedOnPublicHolidays, actor);
    }
}
