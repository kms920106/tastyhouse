package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

@Service
@Transactional
class ShopPhotoCategoryDeleteService implements ShopPhotoCategoryDeleteUseCase {

    private final ShopDetailPersistencePort shopDetailPersistencePort;

    public ShopPhotoCategoryDeleteService(ShopDetailPersistencePort shopDetailPersistencePort) {
        this.shopDetailPersistencePort = shopDetailPersistencePort;
    }

    @Override
    public void deletePhotoCategory(ShopPhotoCategoryDeleteCommand command) {
        Long categoryId = command.categoryId();

        shopDetailPersistencePort.deletePhotoCategoryById(categoryId);
    }
}
