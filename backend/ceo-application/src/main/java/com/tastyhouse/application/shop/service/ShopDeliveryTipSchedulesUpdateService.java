package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipScheduleSpec;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipSchedulesUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipSchedulesUpdateUseCase;

@Service
@Transactional
class ShopDeliveryTipSchedulesUpdateService implements ShopDeliveryTipSchedulesUpdateUseCase {

    private final ShopDeliveryTipService shopDeliveryTipService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipSchedulesUpdateService(ShopDeliveryTipService shopDeliveryTipService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryTipService = shopDeliveryTipService;
        this.shopOwnershipValidator = shopOwnershipValidator;
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
}
