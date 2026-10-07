package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopFoodTypeListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;

@Service
@Transactional(readOnly = true)
class ShopFoodTypeListQueryService implements ShopFoodTypeListQueryUseCase {

    private final ShopQueryPort shopQueryPort;

    public ShopFoodTypeListQueryService(ShopQueryPort shopQueryPort) {
        this.shopQueryPort = shopQueryPort;
    }

    @Override
    public List<ShopFoodTypeCategoryResult> searchAllFoodTypes() {
        return shopQueryPort.findVisibleFoodTypeCategories();
    }
}
