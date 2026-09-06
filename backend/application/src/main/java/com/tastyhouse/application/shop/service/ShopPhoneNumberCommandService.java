package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopPhoneNumberRegistryService;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberPrimaryDesignateCommand;

@Service
@CeoApp
@Transactional
public class ShopPhoneNumberCommandService implements ShopPhoneNumberCommandUseCase {

    private final ShopPhoneNumberRegistryService shopPhoneNumberRegistryService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopPhoneNumberCommandService(ShopPhoneNumberRegistryService shopPhoneNumberRegistryService, ShopOwnershipValidator shopOwnershipValidator) {
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

    @Override
    public void deletePhoneNumber(ShopPhoneNumberDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long phoneNumberId = command.phoneNumberId();

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopPhoneNumberRegistryService.deletePhoneNumber(phoneNumberId, actor);
    }

    @Override
    public void designatePrimary(ShopPhoneNumberPrimaryDesignateCommand command) {
        Long ceoId = command.ceoId();
        Long phoneNumberId = command.phoneNumberId();

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopPhoneNumberRegistryService.designatePrimary(phoneNumberId, actor);
    }
}
