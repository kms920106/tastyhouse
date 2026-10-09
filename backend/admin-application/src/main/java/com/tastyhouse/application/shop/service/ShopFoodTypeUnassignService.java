package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopFoodTypeUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeUnassignUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeSavePort;

@Service
@Transactional
class ShopFoodTypeUnassignService implements ShopFoodTypeUnassignUseCase {

    private final ShopFoodTypeSavePort shopFoodTypeSavePort;

    public ShopFoodTypeUnassignService(ShopFoodTypeSavePort shopFoodTypeSavePort) {
        this.shopFoodTypeSavePort = shopFoodTypeSavePort;
    }

    @Override
    public void unassignFoodType(ShopFoodTypeUnassignCommand command) {
        Long id = command.shopId();
        Long foodTypeCategoryId = command.foodTypeCategoryId();

        shopFoodTypeSavePort.deleteFoodTypeByShopIdAndCategoryId(id, foodTypeCategoryId);
    }
}
