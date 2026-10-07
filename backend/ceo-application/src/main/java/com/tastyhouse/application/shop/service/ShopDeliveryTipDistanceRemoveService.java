package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipDistanceRemoveCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipDistanceRemoveUseCase;

@Service
@Transactional
class ShopDeliveryTipDistanceRemoveService implements ShopDeliveryTipDistanceRemoveUseCase {

    private final ShopDeliveryTipService shopDeliveryTipService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipDistanceRemoveService(ShopDeliveryTipService shopDeliveryTipService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryTipService = shopDeliveryTipService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void removeDistanceTip(ShopDeliveryTipDistanceRemoveCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.clearDistanceTip(targetShopId, actor);
    }
}
