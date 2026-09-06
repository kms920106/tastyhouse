package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.service.ShopConvenienceInfoService;
import com.tastyhouse.application.shop.port.in.ShopAmenityOwnerAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityOwnerUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopConvenienceInfoCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopConvenienceInfoUpdateCommand;

@Service
@CeoApp
@Transactional
public class ShopConvenienceInfoCommandService implements ShopConvenienceInfoCommandUseCase {

    private final ShopConvenienceInfoService shopConvenienceInfoService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopConvenienceInfoCommandService(
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

    @Override
    public Long assignAmenity(ShopAmenityOwnerAssignCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopConvenienceInfoService.assignAmenity(shopId, amenityCategoryId, ShopChangeActor.ceo(ceoId));
    }

    @Override
    public void unassignAmenity(ShopAmenityOwnerUnassignCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopConvenienceInfoService.unassignAmenity(shopId, amenityCategoryId, ShopChangeActor.ceo(ceoId));
    }
}
