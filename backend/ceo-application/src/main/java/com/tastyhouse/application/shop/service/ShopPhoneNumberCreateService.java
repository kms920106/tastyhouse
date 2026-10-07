package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberCreateUseCase;

@Service
@Transactional
class ShopPhoneNumberCreateService implements ShopPhoneNumberCreateUseCase {

    private final ShopPhoneNumberRegistryService shopPhoneNumberRegistryService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopPhoneNumberCreateService(ShopPhoneNumberRegistryService shopPhoneNumberRegistryService, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopPhoneNumberRegistryService = shopPhoneNumberRegistryService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long addPhoneNumber(ShopPhoneNumberCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String phoneNumber = command.phoneNumber();
        boolean virtual = command.virtual();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        return shopPhoneNumberRegistryService.addPhoneNumber(shopId, phoneNumber, virtual, actor);
    }
}
