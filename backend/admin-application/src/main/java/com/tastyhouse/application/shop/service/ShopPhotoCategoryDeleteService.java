package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailSavePort;

@Service
@Transactional
class ShopPhotoCategoryDeleteService implements ShopPhotoCategoryDeleteUseCase {

    private final ShopDetailSavePort shopDetailSavePort;

    public ShopPhotoCategoryDeleteService(ShopDetailSavePort shopDetailSavePort) {
        this.shopDetailSavePort = shopDetailSavePort;
    }

    @Override
    public void deletePhotoCategory(ShopPhotoCategoryDeleteCommand command) {
        Long categoryId = command.categoryId();

        shopDetailSavePort.deletePhotoCategoryById(categoryId);
    }
}
