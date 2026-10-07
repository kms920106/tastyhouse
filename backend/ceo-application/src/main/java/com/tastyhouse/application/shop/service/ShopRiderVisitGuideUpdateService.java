package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideUpdateUseCase;

@Service
@Transactional
class ShopRiderVisitGuideUpdateService implements ShopRiderVisitGuideUpdateUseCase {

    private final ShopRiderGuideService shopRiderGuideService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderVisitGuideUpdateService(
        ShopRiderGuideService shopRiderGuideService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideService = shopRiderGuideService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateVisitGuide(ShopRiderVisitGuideUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String visitGuide = command.visitGuide();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.updateVisitGuide(shopId, visitGuide, RiderGuideActorType.CEO, ceoId);
    }
}
