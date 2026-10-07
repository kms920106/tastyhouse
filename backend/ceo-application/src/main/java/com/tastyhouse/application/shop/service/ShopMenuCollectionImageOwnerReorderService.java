package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageOwnerReorderUseCase;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageReorderCommand;

@Service
@Transactional
class ShopMenuCollectionImageOwnerReorderService implements ShopMenuCollectionImageOwnerReorderUseCase {

    private final ShopMenuCollectionImageService shopMenuCollectionImageService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopMenuCollectionImageOwnerReorderService(
        ShopMenuCollectionImageService shopMenuCollectionImageService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopMenuCollectionImageService = shopMenuCollectionImageService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void reorderMenuCollectionImages(ShopMenuCollectionImageReorderCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> imageIds = command.imageIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopId id = ShopId.of(shopId);
        shopMenuCollectionImageService.reorder(id, imageIds);
    }
}
