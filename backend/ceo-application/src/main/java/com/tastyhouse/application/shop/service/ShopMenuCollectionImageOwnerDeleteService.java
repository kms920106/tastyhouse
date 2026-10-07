package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageOwnerDeleteUseCase;

@Service
@Transactional
class ShopMenuCollectionImageOwnerDeleteService implements ShopMenuCollectionImageOwnerDeleteUseCase {

    private final ShopMenuCollectionImageService shopMenuCollectionImageService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopMenuCollectionImageOwnerDeleteService(
        ShopMenuCollectionImageService shopMenuCollectionImageService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopMenuCollectionImageService = shopMenuCollectionImageService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void deleteMenuCollectionImage(ShopMenuCollectionImageDeleteCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long imageId = command.imageId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId id = ShopId.of(shopId);
        ShopMenuCollectionImageId menuCollectionImageId = ShopMenuCollectionImageId.of(imageId);
        shopMenuCollectionImageService.delete(id, menuCollectionImageId);
    }
}
