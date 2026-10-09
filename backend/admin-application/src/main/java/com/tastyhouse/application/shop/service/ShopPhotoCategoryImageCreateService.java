package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.vo.ShopPhotoCategoryId;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailSavePort;

@Service
@Transactional
class ShopPhotoCategoryImageCreateService implements ShopPhotoCategoryImageCreateUseCase {

    private final ShopDetailSavePort shopDetailSavePort;

    public ShopPhotoCategoryImageCreateService(ShopDetailSavePort shopDetailSavePort) {
        this.shopDetailSavePort = shopDetailSavePort;
    }

    @Override
    public Long createPhotoCategoryImage(ShopPhotoCategoryImageCreateCommand command) {
        Long categoryId = command.categoryId();
        Long imageFileId = command.imageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

        ShopPhotoCategoryImage image = shopDetailSavePort.savePhotoCategoryImage(
            ShopPhotoCategoryImage.of(
                ShopPhotoCategoryId.of(categoryId),
                UploadedFileId.of(imageFileId),
                sort,
                visible
            )
        );
        return image.getId();
    }
}
