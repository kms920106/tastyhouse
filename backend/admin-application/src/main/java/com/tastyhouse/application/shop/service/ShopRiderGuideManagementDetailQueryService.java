package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopRiderGuideManagementDetailQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;

@Service
@Transactional(readOnly = true)
class ShopRiderGuideManagementDetailQueryService implements ShopRiderGuideManagementDetailQueryUseCase {

    private final ShopRiderGuideManagementQueryPort shopRiderGuideManagementQueryPort;

    public ShopRiderGuideManagementDetailQueryService(ShopRiderGuideManagementQueryPort shopRiderGuideManagementQueryPort) {
        this.shopRiderGuideManagementQueryPort = shopRiderGuideManagementQueryPort;
    }

    @Override
    public ShopRiderGuideDetail getRiderGuide(Long shopId) {
        ShopRiderGuideResult result = shopRiderGuideManagementQueryPort.findRiderGuide(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));

        return new ShopRiderGuideDetail(result, shopRiderGuideManagementQueryPort.findHistories(shopId));
    }
}
