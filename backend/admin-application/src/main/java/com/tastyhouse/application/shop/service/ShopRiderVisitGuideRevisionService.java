package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideRevisionCommand;
import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideRevisionUseCase;

@Service
@Transactional
class ShopRiderVisitGuideRevisionService implements ShopRiderVisitGuideRevisionUseCase {

    private final ShopRiderGuideService shopRiderGuideService;

    public ShopRiderVisitGuideRevisionService(ShopRiderGuideService shopRiderGuideService) {
        this.shopRiderGuideService = shopRiderGuideService;
    }

    @Override
    public Long requestRevision(ShopRiderVisitGuideRevisionCommand command) {
        Long shopId = command.shopId();
        Long adminId = command.adminId();
        String reason = command.reason();

        return shopRiderGuideService.requestRevision(shopId, adminId, reason);
    }
}
