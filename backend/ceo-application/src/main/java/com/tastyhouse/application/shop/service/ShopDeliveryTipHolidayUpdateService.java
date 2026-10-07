package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipHolidayUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipHolidayUpdateUseCase;

@Service
@Transactional
class ShopDeliveryTipHolidayUpdateService implements ShopDeliveryTipHolidayUpdateUseCase {

    private final ShopDeliveryTipService shopDeliveryTipService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipHolidayUpdateService(ShopDeliveryTipService shopDeliveryTipService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryTipService = shopDeliveryTipService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateHolidayTip(ShopDeliveryTipHolidayUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        int tipAmount = command.tipAmount();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.changeHolidayTip(targetShopId, tipAmount, actor);
    }
}
