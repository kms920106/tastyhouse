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
import com.tastyhouse.application.shop.port.out.write.ShopDetailLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDetailSavePort;

@Service
@Transactional
class ShopFoodTypeAssignService implements ShopFoodTypeAssignUseCase {

    private final ShopDetailLoadPort shopDetailLoadPort;
    private final ShopDetailSavePort shopDetailSavePort;

    public ShopFoodTypeAssignService(ShopDetailLoadPort shopDetailLoadPort, ShopDetailSavePort shopDetailSavePort) {
        this.shopDetailLoadPort = shopDetailLoadPort;
        this.shopDetailSavePort = shopDetailSavePort;
    }

    @Override
    public Long assignFoodType(ShopFoodTypeAssignCommand command) {
        Long id = command.shopId();
        Long foodTypeCategoryId = command.foodTypeCategoryId();

        shopDetailLoadPort.findFoodTypeCategoryById(foodTypeCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
        ShopFoodType foodType = shopDetailSavePort.saveFoodType(ShopFoodType.of(ShopId.of(id), ShopFoodTypeCategoryId.of(foodTypeCategoryId)));
        return foodType.getId();
    }
}
