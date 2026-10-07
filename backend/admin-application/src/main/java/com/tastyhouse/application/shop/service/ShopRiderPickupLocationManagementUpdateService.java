package com.tastyhouse.application.shop.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.application.shop.port.in.ShopRiderPickupLocationManagementUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderPickupLocationManagementUpdateUseCase;

@Service
@Transactional
class ShopRiderPickupLocationManagementUpdateService implements ShopRiderPickupLocationManagementUpdateUseCase {

    private final ShopRiderGuideService shopRiderGuideService;

    public ShopRiderPickupLocationManagementUpdateService(ShopRiderGuideService shopRiderGuideService) {
        this.shopRiderGuideService = shopRiderGuideService;
    }

    @Override
    public void updatePickupLocation(ShopRiderPickupLocationManagementUpdateCommand command) {
        Long shopId = command.shopId();
        Long adminId = command.adminId();
        String roadAddress = command.roadAddress();
        String lotAddress = command.lotAddress();
        String detailAddress = command.detailAddress();
        BigDecimal latitude = command.latitude();
        BigDecimal longitude = command.longitude();

        shopRiderGuideService.updatePickupLocation(
            shopId, roadAddress, lotAddress, detailAddress, latitude, longitude, RiderGuideActorType.ADMIN, adminId
        );
    }
}
