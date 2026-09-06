package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageResult;
import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopMenuCollectionImageOwnerQueryService implements ShopMenuCollectionImageOwnerQueryUseCase {

    private final ShopOwnerQueryPort shopOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopMenuCollectionImageOwnerQueryService(
        ShopOwnerQueryPort shopOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopOwnerQueryPort = shopOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopMenuCollectionImageResult> getMenuCollectionImages(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopOwnerQueryPort.findMenuCollectionImages(shopId);
    }

}
