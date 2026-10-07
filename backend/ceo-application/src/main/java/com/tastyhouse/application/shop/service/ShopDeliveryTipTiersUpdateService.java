package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipTierSpec;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipTiersUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryTipTiersUpdateUseCase;

@Service
@Transactional
class ShopDeliveryTipTiersUpdateService implements ShopDeliveryTipTiersUpdateUseCase {

    private final ShopDeliveryTipService shopDeliveryTipService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopDeliveryTipTiersUpdateService(ShopDeliveryTipService shopDeliveryTipService, ShopOwnershipValidator shopOwnershipValidator) {
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
}
