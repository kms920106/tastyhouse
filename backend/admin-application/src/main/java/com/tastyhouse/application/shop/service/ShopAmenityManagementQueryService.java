package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopAmenityManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;

@Service
@Transactional(readOnly = true)
class ShopAmenityManagementQueryService implements ShopAmenityManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;

    public ShopAmenityManagementQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
    }

    @Override
    public List<ShopAmenityAssignmentResult> getShopAmenities(Long id) {
        return shopBasicInfoQueryPort.findAmenityAssignments(id);
    }
}
