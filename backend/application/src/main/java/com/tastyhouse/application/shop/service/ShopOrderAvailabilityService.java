package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusResult;
import com.tastyhouse.domain.shop.service.ShopOrderMethodAvailability;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

public class ShopOrderAvailabilityService {

    private final ShopOperatingStatusService shopOperatingStatusService;
    private final ShopDetailPersistencePort shopDetailPersistencePort;

    public ShopOrderAvailabilityService(
        ShopOperatingStatusService shopOperatingStatusService,
        ShopDetailPersistencePort shopDetailPersistencePort
    ) {
        this.shopOperatingStatusService = shopOperatingStatusService;
        this.shopDetailPersistencePort = shopDetailPersistencePort;
    }

    public void validateOrderable(Shop shop, OrderMethod orderMethod, LocalDateTime at) {
        ShopOrderMethodAvailability availability =
            shopOperatingStatusService.findOrderAvailability(shop, orderMethod, at);

        ShopOperatingStatusResult shopStatus = availability.shopWide();
        if (!shopStatus.isOpen()) {
            throw new BusinessException(ErrorCode.SHOP_NOT_ORDERABLE,
                ErrorCode.SHOP_NOT_ORDERABLE.getDefaultMessage()
                    + ": " + shopStatus.unavailableReason().getDisplayName());
        }

        if (!isAssigned(shop.getId(), orderMethod)) {
            throw new BusinessException(ErrorCode.SHOP_ORDER_METHOD_NOT_SUPPORTED,
                ErrorCode.SHOP_ORDER_METHOD_NOT_SUPPORTED.getDefaultMessage()
                    + ": " + orderMethod.getDisplayName());
        }

        ShopOperatingStatusResult methodStatus = availability.orderMethod();
        if (!methodStatus.isOpen()) {
            throw new BusinessException(ErrorCode.SHOP_ORDER_METHOD_SUSPENDED,
                ErrorCode.SHOP_ORDER_METHOD_SUSPENDED.getDefaultMessage()
                    + ": " + orderMethod.getDisplayName()
                    + " (" + methodStatus.unavailableReason().getDisplayName() + ")");
        }
    }

    private boolean isAssigned(Long shopId, OrderMethod orderMethod) {
        return shopDetailPersistencePort.findOrderMethodsByShopId(shopId).stream()
            .map(ShopOrderMethod::getOrderMethod)
            .anyMatch(assigned -> assigned == orderMethod);
    }
}
