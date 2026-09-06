package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopOriginInfoOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopOriginInfoOwnerQueryService implements ShopOriginInfoOwnerQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOriginInfoOwnerQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Optional<ShopOriginInfoResult> getOriginInfo(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findOriginInfo(shopId);
    }

}
