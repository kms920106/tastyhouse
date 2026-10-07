package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;

public interface ShopFoodTypeManagementQueryUseCase {

    List<ShopFoodTypeAssignmentResult> getShopFoodTypes(Long id);
}
