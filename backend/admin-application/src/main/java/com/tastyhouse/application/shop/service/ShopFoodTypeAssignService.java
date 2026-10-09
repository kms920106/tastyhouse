package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeAssignUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopFoodTypeSavePort;

@Service
@Transactional
class ShopFoodTypeAssignService implements ShopFoodTypeAssignUseCase {

    private final ShopFoodTypeLoadPort shopFoodTypeLoadPort;
    private final ShopFoodTypeSavePort shopFoodTypeSavePort;

    public ShopFoodTypeAssignService(ShopFoodTypeLoadPort shopFoodTypeLoadPort, ShopFoodTypeSavePort shopFoodTypeSavePort) {
        this.shopFoodTypeLoadPort = shopFoodTypeLoadPort;
        this.shopFoodTypeSavePort = shopFoodTypeSavePort;
    }

    @Override
    public Long assignFoodType(ShopFoodTypeAssignCommand command) {
        Long id = command.shopId();
        Long foodTypeCategoryId = command.foodTypeCategoryId();

        shopFoodTypeLoadPort.findFoodTypeCategoryById(foodTypeCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
        ShopFoodType foodType = shopFoodTypeSavePort.saveFoodType(ShopFoodType.of(ShopId.of(id), ShopFoodTypeCategoryId.of(foodTypeCategoryId)));
        return foodType.getId();
    }
}
