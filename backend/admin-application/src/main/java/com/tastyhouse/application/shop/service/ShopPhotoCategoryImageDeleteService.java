package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailSavePort;

@Service
@Transactional
class ShopPhotoCategoryImageDeleteService implements ShopPhotoCategoryImageDeleteUseCase {

    private final ShopDetailSavePort shopDetailSavePort;

    public ShopPhotoCategoryImageDeleteService(ShopDetailSavePort shopDetailSavePort) {
        this.shopDetailSavePort = shopDetailSavePort;
    }

    @Override
    public void deletePhotoCategoryImage(ShopPhotoCategoryImageDeleteCommand command) {
        Long imageId = command.imageId();

        shopDetailSavePort.deletePhotoCategoryImageById(imageId);
    }
}
