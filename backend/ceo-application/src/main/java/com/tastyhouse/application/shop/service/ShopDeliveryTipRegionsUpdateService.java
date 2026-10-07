package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipRegionSpec;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipRegionsUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipRegionsUpdateUseCase;

@Service
@Transactional
class ShopDeliveryTipRegionsUpdateService implements ShopDeliveryTipRegionsUpdateUseCase {

    private final ShopDeliveryTipService shopDeliveryTipService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipRegionsUpdateService(ShopDeliveryTipService shopDeliveryTipService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopDeliveryTipService = shopDeliveryTipService;
        this.shopOwnershipValidator = shopOwnershipValidator;
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
}
