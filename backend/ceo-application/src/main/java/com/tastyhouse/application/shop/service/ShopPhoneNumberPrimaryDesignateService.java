package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberPrimaryDesignateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhoneNumberPrimaryDesignateUseCase;

@Service
@Transactional
class ShopPhoneNumberPrimaryDesignateService implements ShopPhoneNumberPrimaryDesignateUseCase {

    private final ShopPhoneNumberRegistryService shopPhoneNumberRegistryService;

    public ShopPhoneNumberPrimaryDesignateService(ShopPhoneNumberRegistryService shopPhoneNumberRegistryService) {
        this.shopPhoneNumberRegistryService = shopPhoneNumberRegistryService;
    }

    @Override
    public void designatePrimary(ShopPhoneNumberPrimaryDesignateCommand command) {
        Long ceoId = command.ceoId();
        Long phoneNumberId = command.phoneNumberId();

        ShopChangeActor actor = ShopChangeActor.ceo(ceoId);
        shopPhoneNumberRegistryService.designatePrimary(phoneNumberId, actor);
    }
}
