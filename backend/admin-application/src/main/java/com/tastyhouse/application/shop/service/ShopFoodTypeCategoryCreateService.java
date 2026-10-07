package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryCreateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

@Service
@Transactional
class ShopFoodTypeCategoryCreateService implements ShopFoodTypeCategoryCreateUseCase {

    private final ShopDetailPersistencePort shopDetailPersistencePort;

    public ShopFoodTypeCategoryCreateService(ShopDetailPersistencePort shopDetailPersistencePort) {
        this.shopDetailPersistencePort = shopDetailPersistencePort;
    }

    @Override
    public Long createFoodTypeCategory(ShopFoodTypeCategoryCreateCommand command) {
        String foodType = command.foodType();
        String displayName = command.displayName();
        Long activeImageFileId = command.activeImageFileId();
        Long inactiveImageFileId = command.inactiveImageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

        ShopFoodTypeCategory foodTypeCategory = ShopFoodTypeCategory.of(
            FoodType.from(foodType),
            displayName,
            UploadedFileId.of(activeImageFileId),
            UploadedFileId.of(inactiveImageFileId),
            sort,
            visible
        );
        return shopDetailPersistencePort.saveFoodTypeCategory(foodTypeCategory).getId();
    }
}
