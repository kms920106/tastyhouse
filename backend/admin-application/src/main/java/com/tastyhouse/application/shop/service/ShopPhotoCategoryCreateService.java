package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailSavePort;

@Service
@Transactional
class ShopPhotoCategoryCreateService implements ShopPhotoCategoryCreateUseCase {

    private final ShopDetailSavePort shopDetailSavePort;

    public ShopPhotoCategoryCreateService(ShopDetailSavePort shopDetailSavePort) {
        this.shopDetailSavePort = shopDetailSavePort;
    }

    @Override
    public Long createPhotoCategory(ShopPhotoCategoryCreateCommand command) {
        Long id = command.shopId();
        String name = command.name();

        ShopPhotoCategory photoCategory = shopDetailSavePort.savePhotoCategory(ShopPhotoCategory.of(ShopId.of(id), name));
        return photoCategory.getId();
    }
}
