package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.application.shop.port.in.ShopRiderPickupLocationOwnerUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderPickupLocationOwnerUpdateUseCase;

@Service
@Transactional
class ShopRiderPickupLocationOwnerUpdateService implements ShopRiderPickupLocationOwnerUpdateUseCase {

    private final ShopRiderGuideService shopRiderGuideService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderPickupLocationOwnerUpdateService(
        ShopRiderGuideService shopRiderGuideService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideService = shopRiderGuideService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updatePickupLocation(ShopRiderPickupLocationOwnerUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String roadAddress = command.roadAddress();
        String lotAddress = command.lotAddress();
        String detailAddress = command.detailAddress();
        BigDecimal latitude = command.latitude();
        BigDecimal longitude = command.longitude();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.updatePickupLocation(
            shopId, roadAddress, lotAddress, detailAddress, latitude, longitude, RiderGuideActorType.CEO, ceoId
        );
    }
}
