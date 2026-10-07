package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopRiderGuideOwnerDetailQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;

@Service
@Transactional(readOnly = true)
class ShopRiderGuideOwnerDetailQueryService implements ShopRiderGuideOwnerDetailQueryUseCase {

    private final ShopRiderGuideQueryPort shopRiderGuideQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderGuideOwnerDetailQueryService(
        ShopRiderGuideQueryPort shopRiderGuideQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideQueryPort = shopRiderGuideQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopRiderGuideResult getRiderGuide(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopRiderGuideQueryPort.findRiderGuide(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
    }
}
