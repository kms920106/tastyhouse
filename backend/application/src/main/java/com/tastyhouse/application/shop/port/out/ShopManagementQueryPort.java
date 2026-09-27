package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopManagementQueryPort {

    PageResult<ShopContentBoardResult> findContentBoardPage(Long shopId, Boolean hidden, String contentType, PageQuery pageQuery);

    PageResult<ShopImageChangeRequestResult> findImageChangeRequestPage(String status, String imageType, PageQuery pageQuery);

    PageResult<ShopMenuCollectionImageRequestResult> findMenuCollectionImageRequestPage(String status, PageQuery pageQuery);

    List<ShopAmenityCategoryResult> findAllAmenityCategories();

    List<ShopFoodTypeCategoryResult> findAllFoodTypeCategories();

    List<ShopFoodTypeAssignmentResult> findFoodTypeAssignments(Long shopId);

    List<ShopPhotoCategoryImageManagementResult> findPhotoCategoryImages(Long shopPhotoCategoryId);

    Optional<ShopManagementDetailResult> findManagementDetailById(Long shopId);
}
