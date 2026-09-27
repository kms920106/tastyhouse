package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

public interface ShopQueryPort {

    List<ShopFoodTypeCategoryResult> findVisibleFoodTypeCategories();

    List<ShopAmenityCategoryResult> findVisibleAmenityCategories();

    List<ShopAmenityWithCategoryResult> findAmenitiesWithCategory(Long shopId);

    List<ShopMenuCollectionImageExposureResult> findMenuCollectionImagesByStatus(Long shopId, String status);

    List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages();

    Optional<ShopVisibleDetailResult> findVisibleDetailById(Long shopId);

    boolean existsBookmark(Long shopId, Long memberId);
}
