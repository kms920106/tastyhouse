package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBusinessHourQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopBusinessHourQueryService implements ShopBusinessHourQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopBusinessHourQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopBusinessHourResult> getBusinessHours(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findBusinessHours(shopId);
    }

    @Override
    public List<ShopBreakTimeResult> getBreakTimes(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findBreakTimes(shopId);
    }
}
