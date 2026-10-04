package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusResult;
import com.tastyhouse.application.shop.port.in.ShopOrderAvailabilityQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderAvailabilityViewResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;

@Service
@Transactional(readOnly = true)
class ShopOrderAvailabilityQueryService implements ShopOrderAvailabilityQueryUseCase {

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
                    entry.getKey().name(),
                    entry.getKey().getDisplayName(),
                    entry.getValue().isOpen(),
                    nameOf(entry.getValue().unavailableReason()),
                    displayNameOf(entry.getValue().unavailableReason())
                ))
                .toList();

        return new ShopOrderAvailabilityViewResult(
            shopStatus.isOpen(),
            nameOf(shopStatus.unavailableReason()),
            displayNameOf(shopStatus.unavailableReason()),
            orderMethods
        );
    }

    @Override
    public List<ShopOrderMethodResult> getOrderMethods(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return ShopCodeDescriptions.ofOrderMethods(shopBasicInfoQueryPort.findOrderMethods(shopId));
    }

    private static String nameOf(OrderUnavailableReason reason) {
        return reason == null ? null : reason.name();
    }

    private static String displayNameOf(OrderUnavailableReason reason) {
        return reason == null ? null : reason.getDisplayName();
    }
}
