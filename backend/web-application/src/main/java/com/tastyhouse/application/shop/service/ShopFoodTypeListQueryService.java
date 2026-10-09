package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopFoodTypeListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopClassificationQueryPort;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;

@Service
@Transactional(readOnly = true)
class ShopFoodTypeListQueryService implements ShopFoodTypeListQueryUseCase {

    private final ShopClassificationQueryPort shopClassificationQueryPort;

    public ShopFoodTypeListQueryService(ShopClassificationQueryPort shopClassificationQueryPort) {
        this.shopClassificationQueryPort = shopClassificationQueryPort;
    }

    @Override
    public List<ShopFoodTypeCategoryResult> searchAllFoodTypes() {
        return shopClassificationQueryPort.findVisibleFoodTypeCategories();
    }
}
