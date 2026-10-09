package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopAmenityOwnerListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopClassificationOwnerQueryPort;

@Service
@Transactional(readOnly = true)
class ShopAmenityOwnerListQueryService implements ShopAmenityOwnerListQueryUseCase {

    private final ShopClassificationOwnerQueryPort shopClassificationOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopAmenityOwnerListQueryService(ShopClassificationOwnerQueryPort shopClassificationOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopClassificationOwnerQueryPort = shopClassificationOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopAmenityAssignmentResult> getAmenities(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopClassificationOwnerQueryPort.findAmenityAssignments(shopId);
    }
}
