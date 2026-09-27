package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ShopDetailStatePort {
    Optional<ShopAmenityCategoryState> findAmenityCategoryById(Long id);

    ShopAmenityCategoryState saveAmenityCategory(ShopAmenityCategoryState amenityCategory);

    Optional<ShopFoodTypeCategoryState> findFoodTypeCategoryById(Long id);

    ShopFoodTypeCategoryState saveFoodTypeCategory(ShopFoodTypeCategoryState foodTypeCategory);

    ShopAmenityState saveAmenity(ShopAmenityState amenity);

    void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId);

    ShopFoodTypeState saveFoodType(ShopFoodTypeState foodType);

    void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId);

    List<ShopBusinessHourState> findBusinessHoursByShopId(Long shopId);

    Optional<ShopBusinessHourState> findBusinessHourById(Long id);

    ShopBusinessHourState saveBusinessHour(ShopBusinessHourState businessHour);

    void deleteBusinessHourById(Long id);

    List<ShopBreakTimeState> findBreakTimesByShopId(Long shopId);

    Optional<ShopBreakTimeState> findBreakTimeById(Long id);

    ShopBreakTimeState saveBreakTime(ShopBreakTimeState breakTime);

    void deleteBreakTimeById(Long id);

    List<ShopClosedDayState> findClosedDaysByShopId(Long shopId);

    Optional<ShopClosedDayState> findClosedDayById(Long id);

    ShopClosedDayState saveClosedDay(ShopClosedDayState closedDay);

    void deleteClosedDayById(Long id);

    List<ShopOrderMethodState> findOrderMethodsByShopId(Long shopId);

    ShopOrderMethodState saveOrderMethod(ShopOrderMethodState orderMethod);

    void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, String orderMethod);

    ShopBannerImageState saveBannerImage(ShopBannerImageState bannerImage);

    void deleteBannerImageById(Long id);

    Optional<ShopPhotoCategoryState> findPhotoCategoryById(Long id);

    ShopPhotoCategoryState savePhotoCategory(ShopPhotoCategoryState photoCategory);

    void deletePhotoCategoryById(Long id);

    Optional<ShopPhotoCategoryImageState> findPhotoCategoryImageById(Long id);

    ShopPhotoCategoryImageState savePhotoCategoryImage(ShopPhotoCategoryImageState photoCategoryImage);

    void deletePhotoCategoryImageById(Long id);

    void saveOwnerMessage(ShopOwnerMessageHistoryState ownerMessageHistory);

    Optional<ShopOwnerMessageHistoryState> findLatestOwnerMessage(Long shopId);
}
