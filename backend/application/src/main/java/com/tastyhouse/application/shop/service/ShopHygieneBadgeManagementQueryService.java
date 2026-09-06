package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.out.ShopHygieneBadgeResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ShopHygieneBadgeManagementQueryService implements ShopHygieneBadgeManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopHygieneBadgeManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopHygieneBadgeResult> getHygieneBadges(Long shopId) {
        return shopBasicInfoQueryPort.findHygieneBadges(shopId);
    }

}
