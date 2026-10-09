package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopChoiceUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopChoiceLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopChoiceSavePort;

@Service
@Transactional
class ShopChoiceUpdateService implements ShopChoiceUpdateUseCase {

    private final ShopChoiceLoadPort shopChoiceLoadPort;
    private final ShopChoiceSavePort shopChoiceSavePort;

    public ShopChoiceUpdateService(ShopChoiceLoadPort shopChoiceLoadPort, ShopChoiceSavePort shopChoiceSavePort) {
        this.shopChoiceLoadPort = shopChoiceLoadPort;
        this.shopChoiceSavePort = shopChoiceSavePort;
    }

    @Override
    public void updateShopChoice(ShopChoiceUpdateCommand command) {
        Long id = command.choiceId();
        String title = command.title();
        String content = command.content();

        ShopChoice shopChoice = shopChoiceLoadPort.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_CHOICE_NOT_FOUND));
        shopChoice.update(title, content);
        shopChoiceSavePort.save(shopChoice);
    }
}
