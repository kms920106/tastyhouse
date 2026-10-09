package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDetailSavePort;

@Service
@Transactional
class ShopPhotoCategoryUpdateService implements ShopPhotoCategoryUpdateUseCase {

    private final ShopDetailLoadPort shopDetailLoadPort;
    private final ShopDetailSavePort shopDetailSavePort;

    public ShopPhotoCategoryUpdateService(ShopDetailLoadPort shopDetailLoadPort, ShopDetailSavePort shopDetailSavePort) {
        this.shopDetailLoadPort = shopDetailLoadPort;
        this.shopDetailSavePort = shopDetailSavePort;
    }

    @Override
    public void updatePhotoCategory(ShopPhotoCategoryUpdateCommand command) {
        Long categoryId = command.categoryId();
        String name = command.name();

        ShopPhotoCategory photoCategory = shopDetailLoadPort.findPhotoCategoryById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_PHOTO_CATEGORY_NOT_FOUND));
        photoCategory.update(name);
        shopDetailSavePort.savePhotoCategory(photoCategory);
    }
}
