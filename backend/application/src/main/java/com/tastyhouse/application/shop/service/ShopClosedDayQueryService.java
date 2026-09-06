package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopClosedDayQueryUseCase;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopTemporaryClosureResult;
import com.tastyhouse.application.shop.port.out.ShopClosedDaysResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopClosedDayQueryService implements ShopClosedDayQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnerQueryPort shopOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopClosedDayQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnerQueryPort shopOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnerQueryPort = shopOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopClosedDaysResult getClosedDays(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<ShopClosedDayResult> regularClosedDays = shopBasicInfoQueryPort.findClosedDays(shopId);
        List<ShopTemporaryClosureResult> temporaryClosures = shopOwnerQueryPort.findTemporaryClosures(shopId);

        return new ShopClosedDaysResult(shop.isClosedOnPublicHolidays(), regularClosedDays, temporaryClosures);
    }
}
