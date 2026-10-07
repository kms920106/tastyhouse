package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopFoodTypeCategoryManagementQueryService implements ShopFoodTypeCategoryManagementQueryUseCase {

    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopFoodTypeCategoryManagementQueryService(ShopManagementQueryPort shopManagementQueryPort) {
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public List<ShopFoodTypeCategoryResult> getFoodTypeCategories() {
        return shopManagementQueryPort.findAllFoodTypeCategories();
    }
}
