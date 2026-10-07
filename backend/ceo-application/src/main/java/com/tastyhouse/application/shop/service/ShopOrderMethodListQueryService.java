package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopOrderMethodListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;

@Service
@Transactional(readOnly = true)
class ShopOrderMethodListQueryService implements ShopOrderMethodListQueryUseCase {

    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopOrderMethodListQueryService(
        ShopOwnershipValidator shopOwnershipValidator,
        ShopBasicInfoQueryPort shopBasicInfoQueryPort
    ) {
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopOrderMethodResult> getOrderMethods(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return ShopCodeDescriptions.ofOrderMethods(shopBasicInfoQueryPort.findOrderMethods(shopId));
    }
}
