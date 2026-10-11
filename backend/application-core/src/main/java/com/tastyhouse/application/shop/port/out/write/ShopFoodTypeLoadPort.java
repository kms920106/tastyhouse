package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;

public interface ShopFoodTypeLoadPort {

    Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id);
}
