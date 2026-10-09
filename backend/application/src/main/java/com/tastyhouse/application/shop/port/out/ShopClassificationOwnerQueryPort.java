package com.tastyhouse.application.shop.port.out;

import java.util.List;

public interface ShopClassificationOwnerQueryPort {

    List<String> findFoodTypeCategoryNames(Long shopId);

    List<ShopAmenityAssignmentResult> findAmenityAssignments(Long shopId);
}
