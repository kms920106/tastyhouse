package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;

public interface ShopFoodTypeListQueryUseCase {

    List<ShopFoodTypeCategoryResult> searchAllFoodTypes();
}
