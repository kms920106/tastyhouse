package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopIntroductionDetailQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOwnerMessageResult;

@Service
@Transactional(readOnly = true)
class ShopIntroductionDetailQueryService implements ShopIntroductionDetailQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionDetailQueryService(
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public String getIntroduction(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findLatestOwnerMessage(shopId)
            .map(ShopOwnerMessageResult::message)
            .orElse(null);
    }
}
