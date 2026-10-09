package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategoryLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategorySavePort;

@Service
@Transactional
class ShopPhotoCategoryUpdateService implements ShopPhotoCategoryUpdateUseCase {

    private final ShopPhotoCategoryLoadPort shopPhotoCategoryLoadPort;
    private final ShopPhotoCategorySavePort shopPhotoCategorySavePort;

    public ShopPhotoCategoryUpdateService(ShopPhotoCategoryLoadPort shopPhotoCategoryLoadPort, ShopPhotoCategorySavePort shopPhotoCategorySavePort) {
        this.shopPhotoCategoryLoadPort = shopPhotoCategoryLoadPort;
        this.shopPhotoCategorySavePort = shopPhotoCategorySavePort;
    }

    @Override
    public void updatePhotoCategory(ShopPhotoCategoryUpdateCommand command) {
        Long categoryId = command.categoryId();
        String name = command.name();

        ShopPhotoCategory photoCategory = shopPhotoCategoryLoadPort.findPhotoCategoryById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_PHOTO_CATEGORY_NOT_FOUND));
        photoCategory.update(name);
        shopPhotoCategorySavePort.savePhotoCategory(photoCategory);
    }
}
