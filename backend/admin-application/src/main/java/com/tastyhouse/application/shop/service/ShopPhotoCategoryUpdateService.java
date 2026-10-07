package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

@Service
@Transactional
class ShopPhotoCategoryUpdateService implements ShopPhotoCategoryUpdateUseCase {

    private final ShopDetailPersistencePort shopDetailPersistencePort;

    public ShopPhotoCategoryUpdateService(ShopDetailPersistencePort shopDetailPersistencePort) {
        this.shopDetailPersistencePort = shopDetailPersistencePort;
    }

    @Override
    public void updatePhotoCategory(ShopPhotoCategoryUpdateCommand command) {
        Long categoryId = command.categoryId();
        String name = command.name();

        ShopPhotoCategory photoCategory = shopDetailPersistencePort.findPhotoCategoryById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_PHOTO_CATEGORY_NOT_FOUND));
        photoCategory.update(name);
        shopDetailPersistencePort.savePhotoCategory(photoCategory);
    }
}
