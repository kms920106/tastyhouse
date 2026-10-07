package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.application.shop.port.in.ShopAmenityOwnerAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityOwnerAssignUseCase;

@Service
@Transactional
class ShopAmenityOwnerAssignService implements ShopAmenityOwnerAssignUseCase {

    private final ShopConvenienceInfoService shopConvenienceInfoService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopAmenityOwnerAssignService(
        ShopConvenienceInfoService shopConvenienceInfoService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopConvenienceInfoService = shopConvenienceInfoService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long assignAmenity(ShopAmenityOwnerAssignCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopConvenienceInfoService.assignAmenity(shopId, amenityCategoryId, ShopChangeActor.ceo(ceoId));
    }
}
