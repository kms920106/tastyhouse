package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopAmenityOwnerUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityOwnerUnassignUseCase;

@Service
@Transactional
class ShopAmenityOwnerUnassignService implements ShopAmenityOwnerUnassignUseCase {

    private final ShopConvenienceInfoService shopConvenienceInfoService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopAmenityOwnerUnassignService(
        ShopConvenienceInfoService shopConvenienceInfoService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopConvenienceInfoService = shopConvenienceInfoService;
        this.shopOwnershipValidator = shopOwnershipValidator;
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
