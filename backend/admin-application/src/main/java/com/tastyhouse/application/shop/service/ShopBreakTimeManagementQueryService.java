package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;

@Service
@Transactional(readOnly = true)
class ShopBreakTimeManagementQueryService implements ShopBreakTimeManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopBreakTimeManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopBreakTimeResult> getBreakTimes(Long id) {
        return ShopCodeDescriptions.ofBreakTimes(shopBasicInfoQueryPort.findBreakTimes(id));
    }
}
