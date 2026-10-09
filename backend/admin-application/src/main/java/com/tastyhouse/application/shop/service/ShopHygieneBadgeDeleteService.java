package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeDeleteUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgeLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgeSavePort;

@Service
@Transactional
class ShopHygieneBadgeDeleteService implements ShopHygieneBadgeDeleteUseCase {

    private final ShopHygieneBadgeLoadPort shopHygieneBadgeLoadPort;
    private final ShopHygieneBadgeSavePort shopHygieneBadgeSavePort;

    public ShopHygieneBadgeDeleteService(ShopHygieneBadgeLoadPort shopHygieneBadgeLoadPort, ShopHygieneBadgeSavePort shopHygieneBadgeSavePort) {
        this.shopHygieneBadgeLoadPort = shopHygieneBadgeLoadPort;
        this.shopHygieneBadgeSavePort = shopHygieneBadgeSavePort;
    }

    @Override
    public void deleteHygieneBadge(ShopHygieneBadgeDeleteCommand command) {
        Long hygieneBadgeId = command.hygieneBadgeId();
        shopHygieneBadgeLoadPort.findById(hygieneBadgeId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_HYGIENE_BADGE_NOT_FOUND));
        shopHygieneBadgeSavePort.deleteById(hygieneBadgeId);
    }
}
