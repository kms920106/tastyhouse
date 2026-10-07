package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberDeleteUseCase;

@Service
@Transactional
class ShopPhoneNumberDeleteService implements ShopPhoneNumberDeleteUseCase {

    private final ShopPhoneNumberRegistryService shopPhoneNumberRegistryService;

    public ShopPhoneNumberDeleteService(ShopPhoneNumberRegistryService shopPhoneNumberRegistryService) {
        this.shopPhoneNumberRegistryService = shopPhoneNumberRegistryService;
    }

    @Override
    public void deletePhoneNumber(ShopPhoneNumberDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long phoneNumberId = command.phoneNumberId();

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopPhoneNumberRegistryService.deletePhoneNumber(phoneNumberId, actor);
    }
}
