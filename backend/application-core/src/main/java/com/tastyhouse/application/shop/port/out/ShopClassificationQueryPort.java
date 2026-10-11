package com.tastyhouse.application.shop.port.out;

import java.util.List;

public interface ShopClassificationQueryPort {

    List<ShopFoodTypeCategoryResult> findVisibleFoodTypeCategories();

    List<ShopAmenityCategoryResult> findVisibleAmenityCategories();

    List<ShopAmenityWithCategoryResult> findAmenitiesWithCategory(Long shopId);
}
