package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopAmenityManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopClassificationManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopAmenityManagementQueryService implements ShopAmenityManagementQueryUseCase {

    private final ShopClassificationManagementQueryPort shopClassificationManagementQueryPort;

    public ShopAmenityManagementQueryService(ShopClassificationManagementQueryPort shopClassificationManagementQueryPort) {
        this.shopClassificationManagementQueryPort = shopClassificationManagementQueryPort;
    }

    @Override
    public List<ShopAmenityAssignmentResult> getShopAmenities(Long id) {
        return shopClassificationManagementQueryPort.findAmenityAssignments(id);
    }
}
