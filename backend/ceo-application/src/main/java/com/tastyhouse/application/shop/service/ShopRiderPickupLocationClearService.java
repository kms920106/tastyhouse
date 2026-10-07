package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.application.shop.port.in.ShopRiderPickupLocationClearCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderPickupLocationClearUseCase;

@Service
@Transactional
class ShopRiderPickupLocationClearService implements ShopRiderPickupLocationClearUseCase {

    private final ShopRiderGuideService shopRiderGuideService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderPickupLocationClearService(
        ShopRiderGuideService shopRiderGuideService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideService = shopRiderGuideService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void clearPickupLocation(ShopRiderPickupLocationClearCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.clearPickupLocation(shopId, RiderGuideActorType.CEO, ceoId);
    }
}
