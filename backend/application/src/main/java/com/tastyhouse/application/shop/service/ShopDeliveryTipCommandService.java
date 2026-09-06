package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipRegionSpec;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipScheduleSpec;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipService;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipTierSpec;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipDistanceRemoveCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipDistanceUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipHolidayUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipRegionsRemoveCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipRegionsUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipSchedulesUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipTiersUpdateCommand;

@Service
@CeoApp
@Transactional
public class ShopDeliveryTipCommandService implements ShopDeliveryTipCommandUseCase {

    private final ShopDeliveryTipService shopDeliveryTipService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipCommandService(ShopDeliveryTipService shopDeliveryTipService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryTipService = shopDeliveryTipService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateTiers(ShopDeliveryTipTiersUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        List<ShopDeliveryTipTierSpec> specs = command.tiers().stream()
            .map(tier -> ShopDeliveryTipTierSpec.of(tier.minOrderAmount(), tier.tipAmount()))
            .toList();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.replaceTiers(targetShopId, specs, actor);
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

    @Override
    public void removeDistanceTip(ShopDeliveryTipDistanceRemoveCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.clearDistanceTip(targetShopId, actor);
    }

    @Override
    public void updateRegionTips(ShopDeliveryTipRegionsUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        List<ShopDeliveryTipRegionSpec> specs = command.regions().stream()
            .map(region -> ShopDeliveryTipRegionSpec.of(region.adminDongId(), region.tipAmount()))
            .toList();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.replaceRegionTips(targetShopId, specs, actor);
    }

    @Override
    public void removeRegionTips(ShopDeliveryTipRegionsRemoveCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.clearRegionTips(targetShopId, actor);
    }

    @Override
    public void updateScheduleTips(ShopDeliveryTipSchedulesUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId targetShopId = shop.getShopId();
        List<ShopDeliveryTipScheduleSpec> specs = command.schedules().stream()
            .map(schedule -> ShopDeliveryTipScheduleSpec.of(
                DayType.from(schedule.dayType()),
                schedule.startTime(),
                schedule.endTime(),
                schedule.tipAmount()
            ))
            .toList();
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopDeliveryTipService.replaceScheduleTips(targetShopId, specs, actor);
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
