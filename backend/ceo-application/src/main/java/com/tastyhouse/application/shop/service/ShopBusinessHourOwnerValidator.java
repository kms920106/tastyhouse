package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

@Component
class ShopBusinessHourOwnerValidator {

    private final ShopDetailPersistencePort shopDetailPersistencePort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopBusinessHourOwnerValidator(
        ShopDetailPersistencePort shopDetailPersistencePort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopDetailPersistencePort = shopDetailPersistencePort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public void validateBusinessHourOwnership(Long ceoId, Long businessHourId) {
        ShopBusinessHour businessHour = shopDetailPersistencePort.findBusinessHourById(businessHourId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, businessHour.getShopId().value());
    }

    public void validateBreakTimeOwnership(Long ceoId, Long breakTimeId) {
        ShopBreakTime breakTime = shopDetailPersistencePort.findBreakTimeById(breakTimeId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        shopOwnershipValidator.validateOwnership(ceoId, breakTime.getShopId().value());
    }
}
