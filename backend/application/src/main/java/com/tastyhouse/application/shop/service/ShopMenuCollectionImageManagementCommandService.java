package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageApproveCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageManagementCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageRejectCommand;

@Service
@AdminApp
@Transactional
public class ShopMenuCollectionImageManagementCommandService implements ShopMenuCollectionImageManagementCommandUseCase {

    private final ShopMenuCollectionImageService shopMenuCollectionImageService;

    public ShopMenuCollectionImageManagementCommandService(ShopMenuCollectionImageService shopMenuCollectionImageService) {
        this.shopMenuCollectionImageService = shopMenuCollectionImageService;
    }

    @Override
    public void approveMenuCollectionImage(ShopMenuCollectionImageApproveCommand command) {
        Long id = command.imageId();
        ShopMenuCollectionImageId imageId = ShopMenuCollectionImageId.of(id);
        shopMenuCollectionImageService.approve(imageId);
    }

    @Override
    public void rejectMenuCollectionImage(ShopMenuCollectionImageRejectCommand command) {
        Long id = command.imageId();
        String rejectReason = command.rejectReason();
        ShopMenuCollectionImageId imageId = ShopMenuCollectionImageId.of(id);
        shopMenuCollectionImageService.reject(imageId, rejectReason);
    }
}
