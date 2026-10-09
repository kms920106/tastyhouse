package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategorySavePort;

@Service
@Transactional
class ShopPhotoCategoryImageDeleteService implements ShopPhotoCategoryImageDeleteUseCase {

    private final ShopPhotoCategorySavePort shopPhotoCategorySavePort;

    public ShopPhotoCategoryImageDeleteService(ShopPhotoCategorySavePort shopPhotoCategorySavePort) {
        this.shopPhotoCategorySavePort = shopPhotoCategorySavePort;
    }

    @Override
    public void deletePhotoCategoryImage(ShopPhotoCategoryImageDeleteCommand command) {
        Long imageId = command.imageId();

        shopPhotoCategorySavePort.deletePhotoCategoryImageById(imageId);
    }
}
