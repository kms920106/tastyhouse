package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopBusinessHourLoadPort;

@Component
class ShopBusinessHourOwnerValidator {

    private final ShopBusinessHourLoadPort shopBusinessHourLoadPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopBusinessHourOwnerValidator(
        ShopBusinessHourLoadPort shopBusinessHourLoadPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBusinessHourLoadPort = shopBusinessHourLoadPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public void validateBusinessHourOwnership(Long ceoId, Long businessHourId) {
        ShopBusinessHour businessHour = shopBusinessHourLoadPort.findBusinessHourById(businessHourId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, businessHour.getShopId().value());
    }

    public void validateBreakTimeOwnership(Long ceoId, Long breakTimeId) {
        ShopBreakTime breakTime = shopBusinessHourLoadPort.findBreakTimeById(breakTimeId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, breakTime.getShopId().value());
    }
}
