package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopChoiceCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopChoiceSavePort;

@Service
@Transactional
class ShopChoiceCreateService implements ShopChoiceCreateUseCase {

    private final ShopChoiceSavePort shopChoiceSavePort;

    public ShopChoiceCreateService(ShopChoiceSavePort shopChoiceSavePort) {
        this.shopChoiceSavePort = shopChoiceSavePort;
    }

    @Override
    public Long createShopChoice(ShopChoiceCreateCommand command) {
        Long shopId = command.shopId();
        String title = command.title();
        String content = command.content();

        ShopChoice shopChoice = shopChoiceSavePort.save(ShopChoice.of(ShopId.of(shopId), title, content));
        return shopChoice.getId();
    }
}
