package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;

@Service
@Transactional(readOnly = true)
class ShopBusinessHourManagementQueryService implements ShopBusinessHourManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopBusinessHourManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopBusinessHourResult> getBusinessHours(Long id) {
        return ShopCodeDescriptions.ofBusinessHours(shopBasicInfoQueryPort.findBusinessHours(id));
    }
}
