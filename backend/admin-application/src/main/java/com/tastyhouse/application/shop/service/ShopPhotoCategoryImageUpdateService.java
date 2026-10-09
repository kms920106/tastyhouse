package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategoryLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategorySavePort;

@Service
@Transactional
class ShopPhotoCategoryImageUpdateService implements ShopPhotoCategoryImageUpdateUseCase {

    private final ShopPhotoCategoryLoadPort shopPhotoCategoryLoadPort;
    private final ShopPhotoCategorySavePort shopPhotoCategorySavePort;

    public ShopPhotoCategoryImageUpdateService(ShopPhotoCategoryLoadPort shopPhotoCategoryLoadPort, ShopPhotoCategorySavePort shopPhotoCategorySavePort) {
        this.shopPhotoCategoryLoadPort = shopPhotoCategoryLoadPort;
        this.shopPhotoCategorySavePort = shopPhotoCategorySavePort;
    }

    @Override
    public void updatePhotoCategoryImage(ShopPhotoCategoryImageUpdateCommand command) {
        Long imageId = command.imageId();
        Long imageFileId = command.imageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

        ShopPhotoCategoryImage image = shopPhotoCategoryLoadPort.findPhotoCategoryImageById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_PHOTO_CATEGORY_IMAGE_NOT_FOUND));
        image.update(UploadedFileId.of(imageFileId), sort, visible);
        shopPhotoCategorySavePort.savePhotoCategoryImage(image);
    }
}
