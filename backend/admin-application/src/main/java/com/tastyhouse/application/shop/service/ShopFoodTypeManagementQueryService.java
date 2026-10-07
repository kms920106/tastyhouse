package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopFoodTypeManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopFoodTypeManagementQueryService implements ShopFoodTypeManagementQueryUseCase {

    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopFoodTypeManagementQueryService(ShopManagementQueryPort shopManagementQueryPort) {
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public List<ShopFoodTypeAssignmentResult> getShopFoodTypes(Long id) {
        return shopManagementQueryPort.findFoodTypeAssignments(id);
    }
}
