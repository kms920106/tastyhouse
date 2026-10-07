package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopOrderMethodManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;

@Service
@Transactional(readOnly = true)
class ShopOrderMethodManagementQueryService implements ShopOrderMethodManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopOrderMethodManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopOrderMethodResult> getOrderMethods(Long id) {
        return ShopCodeDescriptions.ofOrderMethods(shopBasicInfoQueryPort.findOrderMethods(id));
    }
}
