package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopClosedDayManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;

@Service
@Transactional(readOnly = true)
class ShopClosedDayManagementQueryService implements ShopClosedDayManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopClosedDayManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopClosedDayResult> getClosedDays(Long id) {
        return ShopCodeDescriptions.ofClosedDays(shopBasicInfoQueryPort.findClosedDays(id));
    }
}
