package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageRejectCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageRejectUseCase;

@Service
@Transactional
class ShopMenuCollectionImageRejectService implements ShopMenuCollectionImageRejectUseCase {

    private final ShopMenuCollectionImageService shopMenuCollectionImageService;

    public ShopMenuCollectionImageRejectService(ShopMenuCollectionImageService shopMenuCollectionImageService) {
        this.shopMenuCollectionImageService = shopMenuCollectionImageService;
    }

    @Override
    public void rejectMenuCollectionImage(ShopMenuCollectionImageRejectCommand command) {
        Long id = command.imageId();
        String rejectReason = command.rejectReason();
        ShopMenuCollectionImageId imageId = ShopMenuCollectionImageId.of(id);
        shopMenuCollectionImageService.reject(imageId, rejectReason);
    }
}
