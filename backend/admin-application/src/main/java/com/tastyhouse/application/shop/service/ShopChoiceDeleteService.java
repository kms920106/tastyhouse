package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopChoiceDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopChoicePersistencePort;

@Service
@Transactional
class ShopChoiceDeleteService implements ShopChoiceDeleteUseCase {

    private final ShopChoicePersistencePort shopChoicePersistencePort;

    public ShopChoiceDeleteService(ShopChoicePersistencePort shopChoicePersistencePort) {
        this.shopChoicePersistencePort = shopChoicePersistencePort;
    }

    @Override
    public void deleteShopChoice(ShopChoiceDeleteCommand command) {
        Long id = command.choiceId();

        shopChoicePersistencePort.deleteById(id);
    }
}
