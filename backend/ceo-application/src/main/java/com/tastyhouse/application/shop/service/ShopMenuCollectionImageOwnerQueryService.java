package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopMediaOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageResult;

@Service
@Transactional(readOnly = true)
class ShopMenuCollectionImageOwnerQueryService implements ShopMenuCollectionImageOwnerQueryUseCase {

    private final ShopMediaOwnerQueryPort shopMediaOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopMenuCollectionImageOwnerQueryService(
        ShopMediaOwnerQueryPort shopMediaOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopMediaOwnerQueryPort = shopMediaOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopMenuCollectionImageResult> getMenuCollectionImages(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopMediaOwnerQueryPort.findMenuCollectionImages(shopId);
    }

}
