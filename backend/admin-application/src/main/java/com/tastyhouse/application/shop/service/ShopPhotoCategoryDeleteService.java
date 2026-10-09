package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategorySavePort;

@Service
@Transactional
class ShopPhotoCategoryDeleteService implements ShopPhotoCategoryDeleteUseCase {

    private final ShopPhotoCategorySavePort shopPhotoCategorySavePort;

    public ShopPhotoCategoryDeleteService(ShopPhotoCategorySavePort shopPhotoCategorySavePort) {
        this.shopPhotoCategorySavePort = shopPhotoCategorySavePort;
    }

    @Override
    public void deletePhotoCategory(ShopPhotoCategoryDeleteCommand command) {
        Long categoryId = command.categoryId();

        shopPhotoCategorySavePort.deletePhotoCategoryById(categoryId);
    }
}
