package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;

public interface ShopDetailRepository {
    Optional<ShopAmenityCategory> findAmenityCategoryById(Long id);

    ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory);

    Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id);

    ShopFoodTypeCategory saveFoodTypeCategory(ShopFoodTypeCategory foodTypeCategory);

    ShopAmenity saveAmenity(ShopAmenity amenity);

    void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId);

    ShopFoodType saveFoodType(ShopFoodType foodType);

    void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId);

    List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId);

    Optional<ShopBusinessHour> findBusinessHourById(Long id);

    ShopBusinessHour saveBusinessHour(ShopBusinessHour businessHour);

    void deleteBusinessHourById(Long id);

    List<ShopBreakTime> findBreakTimesByShopId(Long shopId);

    Optional<ShopBreakTime> findBreakTimeById(Long id);

    ShopBreakTime saveBreakTime(ShopBreakTime breakTime);

    void deleteBreakTimeById(Long id);

    List<ShopClosedDay> findClosedDaysByShopId(Long shopId);

    Optional<ShopClosedDay> findClosedDayById(Long id);

    ShopClosedDay saveClosedDay(ShopClosedDay closedDay);

    void deleteClosedDayById(Long id);

    List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId);

    ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod);

    void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod);

    ShopBannerImage saveBannerImage(ShopBannerImage bannerImage);

    void deleteBannerImageById(Long id);

    Optional<ShopPhotoCategory> findPhotoCategoryById(Long id);

    ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory);

    void deletePhotoCategoryById(Long id);

    Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id);

    ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage);

    void deletePhotoCategoryImageById(Long id);

    void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory);

    Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId);
}
