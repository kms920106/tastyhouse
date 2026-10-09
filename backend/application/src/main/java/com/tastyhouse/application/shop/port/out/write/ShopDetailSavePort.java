package com.tastyhouse.application.shop.port.out.write;

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

public interface ShopDetailSavePort {

    ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory);

    ShopFoodTypeCategory saveFoodTypeCategory(ShopFoodTypeCategory foodTypeCategory);

    ShopAmenity saveAmenity(ShopAmenity amenity);

    void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId);

    ShopFoodType saveFoodType(ShopFoodType foodType);

    void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId);

    ShopBusinessHour saveBusinessHour(ShopBusinessHour businessHour);

    void deleteBusinessHourById(Long id);

    ShopBreakTime saveBreakTime(ShopBreakTime breakTime);

    void deleteBreakTimeById(Long id);

    ShopClosedDay saveClosedDay(ShopClosedDay closedDay);

    void deleteClosedDayById(Long id);

    ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod);

    void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod);

    ShopBannerImage saveBannerImage(ShopBannerImage bannerImage);

    void deleteBannerImageById(Long id);

    ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory);

    void deletePhotoCategoryById(Long id);

    ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage);

    void deletePhotoCategoryImageById(Long id);

    void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory);
}
