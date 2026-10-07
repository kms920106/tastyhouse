package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipDistanceUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipDistanceUpdateUseCase;

@Service
@Transactional
class ShopDeliveryTipDistanceUpdateService implements ShopDeliveryTipDistanceUpdateUseCase {

    private final ShopDeliveryTipService shopDeliveryTipService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipDistanceUpdateService(ShopDeliveryTipService shopDeliveryTipService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryTipService = shopDeliveryTipService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateDistanceTip(ShopDeliveryTipDistanceUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Integer baseDistanceMeters = command.baseDistanceMeters();
        String surchargeUnit = command.surchargeUnit();
        Integer surchargeAmount = command.surchargeAmount();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        DeliveryTipDistanceUnit unit = DeliveryTipDistanceUnit.from(surchargeUnit);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.changeDistanceTip(targetShopId, baseDistanceMeters, unit, surchargeAmount, actor);
    }
}
