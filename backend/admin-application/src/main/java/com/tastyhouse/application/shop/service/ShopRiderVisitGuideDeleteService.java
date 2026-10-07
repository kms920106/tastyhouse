package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideDeleteUseCase;

@Service
@Transactional
class ShopRiderVisitGuideDeleteService implements ShopRiderVisitGuideDeleteUseCase {

    private final ShopRiderGuideService shopRiderGuideService;

    public ShopRiderVisitGuideDeleteService(ShopRiderGuideService shopRiderGuideService) {
        this.shopRiderGuideService = shopRiderGuideService;
    }

    @Override
    public void deleteVisitGuide(ShopRiderVisitGuideDeleteCommand command) {
        Long shopId = command.shopId();
        Long adminId = command.adminId();
        String reason = command.reason();

        shopRiderGuideService.deleteVisitGuide(shopId, adminId, reason);
    }
}
