package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

@Service
@Transactional
class ShopFoodTypeCategoryUpdateService implements ShopFoodTypeCategoryUpdateUseCase {

    private final ShopDetailPersistencePort shopDetailPersistencePort;

    public ShopFoodTypeCategoryUpdateService(ShopDetailPersistencePort shopDetailPersistencePort) {
        this.shopDetailPersistencePort = shopDetailPersistencePort;
    }

    @Override
    public void updateFoodTypeCategory(ShopFoodTypeCategoryUpdateCommand command) {
        Long categoryId = command.categoryId();
        String displayName = command.displayName();
        Long activeImageFileId = command.activeImageFileId();
        Long inactiveImageFileId = command.inactiveImageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

        ShopFoodTypeCategory foodTypeCategory = shopDetailPersistencePort.findFoodTypeCategoryById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
        foodTypeCategory.update(
            displayName,
            UploadedFileId.of(activeImageFileId),
            UploadedFileId.of(inactiveImageFileId),
            sort,
            visible
        );
        shopDetailPersistencePort.saveFoodTypeCategory(foodTypeCategory);
    }
}
