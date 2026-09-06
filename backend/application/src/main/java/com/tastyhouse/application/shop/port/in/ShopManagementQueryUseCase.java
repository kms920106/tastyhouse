package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceDetailResult;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopManagementDetailResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageManagementResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;
import com.tastyhouse.application.shop.port.out.StationResult;
import com.tastyhouse.application.shop.port.out.TagResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface ShopManagementQueryUseCase {

    List<StationResult> getStations();

    PageResult<ShopListItemResult> getShops(
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    );

    ShopDetail getShop(Long id);

    List<ShopBusinessHourResult> getBusinessHours(Long id);

    List<ShopBreakTimeResult> getBreakTimes(Long id);

    List<ShopClosedDayResult> getClosedDays(Long id);

    List<ShopAmenityCategoryResult> getAmenityCategories();

    List<ShopFoodTypeCategoryResult> getFoodTypeCategories();

    List<ShopAmenityAssignmentResult> getShopAmenities(Long id);

    List<ShopFoodTypeAssignmentResult> getShopFoodTypes(Long id);

    List<TagResult> getTags();

    List<ShopOrderMethodResult> getOrderMethods(Long id);

    List<ShopBannerImageResult> getBannerImages(Long id);

    List<ShopPhotoCategoryResult> getPhotoCategories(Long id);

    List<ShopPhotoCategoryImageManagementResult> getPhotoCategoryImages(Long categoryId);

    PageResult<EditorChoiceResult> getShopChoices(int page, int size);

    ShopChoiceDetailResult getShopChoice(Long id);

    record ShopDetail(
        ShopManagementDetailResult shop,
        String thumbnailImageUrl
    ) {
    }
}
