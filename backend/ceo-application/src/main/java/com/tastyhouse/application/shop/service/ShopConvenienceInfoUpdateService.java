package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopConvenienceInfoUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopConvenienceInfoUpdateUseCase;

@Service
@Transactional
class ShopConvenienceInfoUpdateService implements ShopConvenienceInfoUpdateUseCase {

    private final ShopConvenienceInfoService shopConvenienceInfoService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopConvenienceInfoUpdateService(
        ShopConvenienceInfoService shopConvenienceInfoService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopConvenienceInfoService = shopConvenienceInfoService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateConvenienceInfo(ShopConvenienceInfoUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        boolean parkingAvailable = command.parkingAvailable();
        boolean parkingPaid = command.parkingPaid();
        boolean valetAvailable = command.valetAvailable();
        boolean valetPaid = command.valetPaid();
        String directionsGuide = command.directionsGuide();
        BigDecimal displayLatitude = command.displayLatitude();
        BigDecimal displayLongitude = command.displayLongitude();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopConvenienceInfoService.upsertConvenienceInfo(
            shopId,
            parkingAvailable,
            parkingPaid,
            valetAvailable,
            valetPaid,
            directionsGuide,
            displayLatitude,
            displayLongitude,
            ShopChangeActor.ceo(ceoId)
        );
    }
}
