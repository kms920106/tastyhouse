package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.model.ShopImageType;

public interface ShopManagementQueryPort {

    PageResult<ShopContentBoardResult> findContentBoardPage(Long shopId, Boolean hidden, ShopContentType contentType, PageQuery pageQuery);

    PageResult<ShopImageChangeRequestResult> findImageChangeRequestPage(ApprovalStatus status, ShopImageType imageType, PageQuery pageQuery);

    PageResult<ShopMenuCollectionImageRequestResult> findMenuCollectionImageRequestPage(ApprovalStatus status, PageQuery pageQuery);

    List<ShopAmenityCategoryResult> findAllAmenityCategories();

    List<ShopFoodTypeCategoryResult> findAllFoodTypeCategories();

    List<ShopFoodTypeAssignmentResult> findFoodTypeAssignments(Long shopId);

    List<ShopPhotoCategoryImageManagementResult> findPhotoCategoryImages(Long shopPhotoCategoryId);

    Optional<ShopManagementDetailResult> findManagementDetailById(Long shopId);
}
