package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

@Service
@Transactional
class ShopPhotoCategoryImageDeleteService implements ShopPhotoCategoryImageDeleteUseCase {

    private final ShopDetailPersistencePort shopDetailPersistencePort;

    public ShopPhotoCategoryImageDeleteService(ShopDetailPersistencePort shopDetailPersistencePort) {
        this.shopDetailPersistencePort = shopDetailPersistencePort;
    }

    @Override
    public void deletePhotoCategoryImage(ShopPhotoCategoryImageDeleteCommand command) {
        Long imageId = command.imageId();

        shopDetailPersistencePort.deletePhotoCategoryImageById(imageId);
    }
}
