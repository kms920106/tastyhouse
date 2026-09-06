package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopHygieneBadgeOwnerQueryService implements ShopHygieneBadgeOwnerQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopHygieneBadgeOwnerQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopHygieneBadgeResult> getHygieneBadges(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findHygieneBadges(shopId);
    }
}
