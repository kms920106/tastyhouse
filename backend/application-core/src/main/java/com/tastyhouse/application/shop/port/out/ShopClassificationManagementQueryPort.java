package com.tastyhouse.application.shop.port.out;

import java.util.List;

public interface ShopClassificationManagementQueryPort {

    List<ShopAmenityCategoryResult> findAllAmenityCategories();

    List<ShopFoodTypeCategoryResult> findAllFoodTypeCategories();

    List<ShopFoodTypeAssignmentResult> findFoodTypeAssignments(Long shopId);

    List<ShopAmenityAssignmentResult> findAmenityAssignments(Long shopId);
}
