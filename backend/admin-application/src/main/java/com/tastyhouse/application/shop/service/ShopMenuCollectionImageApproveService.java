package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageApproveCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageApproveUseCase;

@Service
@Transactional
class ShopMenuCollectionImageApproveService implements ShopMenuCollectionImageApproveUseCase {

    private final ShopMenuCollectionImageService shopMenuCollectionImageService;

    public ShopMenuCollectionImageApproveService(ShopMenuCollectionImageService shopMenuCollectionImageService) {
        this.shopMenuCollectionImageService = shopMenuCollectionImageService;
    }

    @Override
    public void approveMenuCollectionImage(ShopMenuCollectionImageApproveCommand command) {
        Long id = command.imageId();
        ShopMenuCollectionImageId imageId = ShopMenuCollectionImageId.of(id);
        shopMenuCollectionImageService.approve(imageId);
    }
}
