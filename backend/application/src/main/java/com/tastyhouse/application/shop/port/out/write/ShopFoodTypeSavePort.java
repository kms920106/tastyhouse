package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;

public interface ShopFoodTypeSavePort {

    ShopFoodTypeCategory saveFoodTypeCategory(ShopFoodTypeCategory foodTypeCategory);

    ShopFoodType saveFoodType(ShopFoodType foodType);

    void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId);
}
