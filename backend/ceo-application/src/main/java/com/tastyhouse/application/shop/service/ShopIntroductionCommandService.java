package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopIntroductionCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopIntroductionUpdateCommand;

@Service
@Transactional
public class ShopIntroductionCommandService implements ShopIntroductionCommandUseCase {

    private final ShopLifecycleService shopLifecycleService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionCommandService(ShopLifecycleService shopLifecycleService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopLifecycleService = shopLifecycleService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateIntroduction(ShopIntroductionUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String message = command.message();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopLifecycleService.createOwnerMessage(shopId, message, ShopChangeActor.ceo(ceoId));
    }
}
