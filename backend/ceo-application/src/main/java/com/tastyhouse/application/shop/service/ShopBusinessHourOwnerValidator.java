package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopDetailLoadPort;

@Component
class ShopBusinessHourOwnerValidator {

    private final ShopDetailLoadPort shopDetailLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopBusinessHourOwnerValidator(
        ShopDetailLoadPort shopDetailLoadPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDetailLoadPort = shopDetailLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public void validateBusinessHourOwnership(Long ceoId, Long businessHourId) {
        ShopBusinessHour businessHour = shopDetailLoadPort.findBusinessHourById(businessHourId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, businessHour.getShopId().value());
    }

    public void validateBreakTimeOwnership(Long ceoId, Long breakTimeId) {
        ShopBreakTime breakTime = shopDetailLoadPort.findBreakTimeById(breakTimeId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, breakTime.getShopId().value());
    }
}
