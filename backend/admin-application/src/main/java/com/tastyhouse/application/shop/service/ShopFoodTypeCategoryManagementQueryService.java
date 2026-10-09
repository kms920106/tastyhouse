package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopClassificationManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;

@Service
@Transactional(readOnly = true)
class ShopFoodTypeCategoryManagementQueryService implements ShopFoodTypeCategoryManagementQueryUseCase {

    private final ShopClassificationManagementQueryPort shopClassificationManagementQueryPort;

    public ShopFoodTypeCategoryManagementQueryService(ShopClassificationManagementQueryPort shopClassificationManagementQueryPort) {
        this.shopClassificationManagementQueryPort = shopClassificationManagementQueryPort;
    }

    @Override
    public List<ShopFoodTypeCategoryResult> getFoodTypeCategories() {
        return shopClassificationManagementQueryPort.findAllFoodTypeCategories();
    }
}
