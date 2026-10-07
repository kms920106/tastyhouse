package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopFoodTypeUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeUnassignUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

@Service
@Transactional
class ShopFoodTypeUnassignService implements ShopFoodTypeUnassignUseCase {

    private final ShopDetailPersistencePort shopDetailPersistencePort;

    public ShopFoodTypeUnassignService(ShopDetailPersistencePort shopDetailPersistencePort) {
        this.shopDetailPersistencePort = shopDetailPersistencePort;
    }

    @Override
    public void unassignFoodType(ShopFoodTypeUnassignCommand command) {
        Long id = command.shopId();
        Long foodTypeCategoryId = command.foodTypeCategoryId();

        shopDetailPersistencePort.deleteFoodTypeByShopIdAndCategoryId(id, foodTypeCategoryId);
    }
}
