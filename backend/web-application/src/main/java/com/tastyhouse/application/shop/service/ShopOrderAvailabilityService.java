package com.tastyhouse.application.shop.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusResult;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOrderMethodAvailability;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shop.port.out.write.ShopOrderMethodLoadPort;

@Service
public class ShopOrderAvailabilityService {

    private final ShopOperatingStatusService shopOperatingStatusService;
    private final ShopOrderMethodLoadPort shopOrderMethodLoadPort;

    public ShopOrderAvailabilityService(
        ShopOperatingStatusService shopOperatingStatusService,
        ShopOrderMethodLoadPort shopOrderMethodLoadPort
    ) {
        this.shopOperatingStatusService = shopOperatingStatusService;
        this.shopOrderMethodLoadPort = shopOrderMethodLoadPort;
    }

    public void validateOrderable(Shop shop, OrderMethod orderMethod, LocalDateTime at) {
        ShopOrderMethodAvailability availability =
            shopOperatingStatusService.findOrderAvailability(shop, orderMethod, at);

        ShopOperatingStatusResult shopStatus = availability.shopWide();
        if (!shopStatus.isOpen()) {
            throw new ApplicationException(WebErrorCode.SHOP_NOT_ORDERABLE,
                WebErrorCode.SHOP_NOT_ORDERABLE.getDefaultMessage()
                    + ": " + shopStatus.unavailableReason().getDisplayName());
        }

        if (!isAssigned(shop.getId(), orderMethod)) {
            throw new ApplicationException(WebErrorCode.SHOP_ORDER_METHOD_NOT_SUPPORTED,
                WebErrorCode.SHOP_ORDER_METHOD_NOT_SUPPORTED.getDefaultMessage()
                    + ": " + orderMethod.getDisplayName());
        }

        ShopOperatingStatusResult methodStatus = availability.orderMethod();
        if (!methodStatus.isOpen()) {
            throw new ApplicationException(WebErrorCode.SHOP_ORDER_METHOD_SUSPENDED,
                WebErrorCode.SHOP_ORDER_METHOD_SUSPENDED.getDefaultMessage()
                    + ": " + orderMethod.getDisplayName()
                    + " (" + methodStatus.unavailableReason().getDisplayName() + ")");
        }
    }

    private boolean isAssigned(Long shopId, OrderMethod orderMethod) {
        return shopOrderMethodLoadPort.findOrderMethodsByShopId(shopId).stream()
            .map(ShopOrderMethod::getOrderMethod)
            .anyMatch(assigned -> assigned == orderMethod);
    }
}
