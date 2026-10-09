package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopChoiceDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopChoiceSavePort;

@Service
@Transactional
class ShopChoiceDeleteService implements ShopChoiceDeleteUseCase {

    private final ShopChoiceSavePort shopChoiceSavePort;

    public ShopChoiceDeleteService(ShopChoiceSavePort shopChoiceSavePort) {
        this.shopChoiceSavePort = shopChoiceSavePort;
    }

    @Override
    public void deleteShopChoice(ShopChoiceDeleteCommand command) {
        Long id = command.choiceId();

        shopChoiceSavePort.deleteById(id);
    }
}
