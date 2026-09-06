package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopRiderGuideManagementCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopRiderPickupLocationManagementUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideRevisionCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.service.ShopRiderGuideService;

@Service
@AdminApp
@Transactional
public class ShopRiderGuideManagementCommandService implements ShopRiderGuideManagementCommandUseCase {

    private final ShopRiderGuideService shopRiderGuideService;

    public ShopRiderGuideManagementCommandService(ShopRiderGuideService shopRiderGuideService) {
        this.shopRiderGuideService = shopRiderGuideService;
    }

    @Override
    public void deleteVisitGuide(ShopRiderVisitGuideDeleteCommand command) {
        Long shopId = command.shopId();
        Long adminId = command.adminId();
        String reason = command.reason();

        shopRiderGuideService.deleteVisitGuide(shopId, adminId, reason);
    }

    @Override
    public Long requestRevision(ShopRiderVisitGuideRevisionCommand command) {
        Long shopId = command.shopId();
        Long adminId = command.adminId();
        String reason = command.reason();

        return shopRiderGuideService.requestRevision(shopId, adminId, reason);
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
