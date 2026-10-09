package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDetailSavePort;

@Service
@Transactional
class ShopPhotoCategoryImageUpdateService implements ShopPhotoCategoryImageUpdateUseCase {

    private final ShopDetailLoadPort shopDetailLoadPort;
    private final ShopDetailSavePort shopDetailSavePort;

    public ShopPhotoCategoryImageUpdateService(ShopDetailLoadPort shopDetailLoadPort, ShopDetailSavePort shopDetailSavePort) {
        this.shopDetailLoadPort = shopDetailLoadPort;
        this.shopDetailSavePort = shopDetailSavePort;
    }

    @Override
    public void updatePhotoCategoryImage(ShopPhotoCategoryImageUpdateCommand command) {
        Long imageId = command.imageId();
        Long imageFileId = command.imageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

        ShopPhotoCategoryImage image = shopDetailLoadPort.findPhotoCategoryImageById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_PHOTO_CATEGORY_IMAGE_NOT_FOUND));
        image.update(UploadedFileId.of(imageFileId), sort, visible);
        shopDetailSavePort.savePhotoCategoryImage(image);
    }
}
