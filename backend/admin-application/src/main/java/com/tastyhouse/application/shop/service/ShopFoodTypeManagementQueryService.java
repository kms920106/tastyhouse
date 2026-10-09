package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopFoodTypeManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopClassificationManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;

@Service
@Transactional(readOnly = true)
class ShopFoodTypeManagementQueryService implements ShopFoodTypeManagementQueryUseCase {

    private final ShopClassificationManagementQueryPort shopClassificationManagementQueryPort;

    public ShopFoodTypeManagementQueryService(ShopClassificationManagementQueryPort shopClassificationManagementQueryPort) {
        this.shopClassificationManagementQueryPort = shopClassificationManagementQueryPort;
    }

    @Override
    public List<ShopFoodTypeAssignmentResult> getShopFoodTypes(Long id) {
        return shopClassificationManagementQueryPort.findFoodTypeAssignments(id);
    }
}
