package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.service.ShopRiderGuideService;
import com.tastyhouse.application.shop.port.in.ShopRiderGuideOwnerCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopRiderPickupLocationClearCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderPickupLocationOwnerUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideUpdateCommand;

@Service
@CeoApp
@Transactional
public class ShopRiderGuideOwnerCommandService implements ShopRiderGuideOwnerCommandUseCase {

    private final ShopRiderGuideService shopRiderGuideService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderGuideOwnerCommandService(
        ShopRiderGuideService shopRiderGuideService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideService = shopRiderGuideService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void updateVisitGuide(ShopRiderVisitGuideUpdateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String visitGuide = command.visitGuide();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.updateVisitGuide(shopId, visitGuide, RiderGuideActorType.CEO, ceoId);
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

    @Override
    public void clearPickupLocation(ShopRiderPickupLocationClearCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRiderGuideService.clearPickupLocation(shopId, RiderGuideActorType.CEO, ceoId);
    }
}
