package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopSuspensionQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopSuspensionResult;

/**
 * 점주용 가게 영업 임시중지 조회 서비스(CQRS query 측).
 */
@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopSuspensionQueryService implements ShopSuspensionQueryUseCase {

    private final ShopOwnerQueryPort shopOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopSuspensionQueryService(ShopOwnerQueryPort shopOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopOwnerQueryPort = shopOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopSuspensionResult> getSuspensions(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopOwnerQueryPort.findSuspensions(shopId);
    }

}
