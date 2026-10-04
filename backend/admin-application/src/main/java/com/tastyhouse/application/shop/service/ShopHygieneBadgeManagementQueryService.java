package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;

@Service
@Transactional(readOnly = true)
class ShopHygieneBadgeManagementQueryService implements ShopHygieneBadgeManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopHygieneBadgeManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopHygieneBadgeResult> getHygieneBadges(Long shopId) {
        return shopBasicInfoQueryPort.findHygieneBadges(shopId);
    }

}
