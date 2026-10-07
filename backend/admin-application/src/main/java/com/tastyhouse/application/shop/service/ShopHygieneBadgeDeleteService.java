package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgePersistencePort;

@Service
@Transactional
class ShopHygieneBadgeDeleteService implements ShopHygieneBadgeDeleteUseCase {

    private final ShopHygieneBadgePersistencePort shopHygieneBadgePersistencePort;

    public ShopHygieneBadgeDeleteService(ShopHygieneBadgePersistencePort shopHygieneBadgePersistencePort) {
        this.shopHygieneBadgePersistencePort = shopHygieneBadgePersistencePort;
    }

    @Override
    public void deleteHygieneBadge(ShopHygieneBadgeDeleteCommand command) {
        Long hygieneBadgeId = command.hygieneBadgeId();
        shopHygieneBadgePersistencePort.findById(hygieneBadgeId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_HYGIENE_BADGE_NOT_FOUND));
        shopHygieneBadgePersistencePort.deleteById(hygieneBadgeId);
    }
}
