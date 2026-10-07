package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopChoiceUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopChoicePersistencePort;

@Service
@Transactional
class ShopChoiceUpdateService implements ShopChoiceUpdateUseCase {

    private final ShopChoicePersistencePort shopChoicePersistencePort;

    public ShopChoiceUpdateService(ShopChoicePersistencePort shopChoicePersistencePort) {
        this.shopChoicePersistencePort = shopChoicePersistencePort;
    }

    @Override
    public void updateShopChoice(ShopChoiceUpdateCommand command) {
        Long id = command.choiceId();
        String title = command.title();
        String content = command.content();

        ShopChoice shopChoice = shopChoicePersistencePort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_CHOICE_NOT_FOUND));
        shopChoice.update(title, content);
        shopChoicePersistencePort.save(shopChoice);
    }
}
