package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopOrderAvailabilityQueryUseCase;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusResult;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusService;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.shop.port.out.ShopOrderAvailabilityViewResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopOrderAvailabilityQueryService implements ShopOrderAvailabilityQueryUseCase {

    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopOperatingStatusService shopOperatingStatusService;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopOrderAvailabilityQueryService(
        ShopOwnershipValidator shopOwnershipValidator,
        ShopOperatingStatusService shopOperatingStatusService,
        ShopBasicInfoQueryPort shopBasicInfoQueryPort
    ) {
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopOperatingStatusService = shopOperatingStatusService;
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public ShopOrderAvailabilityViewResult getOrderAvailability(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        LocalDateTime now = LocalDateTime.now();
        ShopOperatingStatusResult shopStatus = shopOperatingStatusService.findOrderAvailability(shopId, now);
        Map<OrderMethod, ShopOperatingStatusResult> methodStatuses =
            shopOperatingStatusService.findOrderMethodAvailabilities(shopId, now);

        List<ShopOrderAvailabilityViewResult.OrderMethodAvailability> orderMethods =
            methodStatuses.entrySet().stream()
                .map(entry -> new ShopOrderAvailabilityViewResult.OrderMethodAvailability(
                    entry.getKey(),
                    entry.getValue().isOpen(),
                    entry.getValue().unavailableReason()
                ))
                .toList();

        return new ShopOrderAvailabilityViewResult(
            shopStatus.isOpen(),
            shopStatus.unavailableReason(),
            orderMethods
        );
    }

    @Override
    public List<ShopOrderMethodResult> getOrderMethods(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findOrderMethods(shopId);
    }
}
