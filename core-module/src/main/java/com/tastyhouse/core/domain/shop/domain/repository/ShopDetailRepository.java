package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenity;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenityCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopBannerImage;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopFoodType;
import com.tastyhouse.core.domain.shop.domain.model.ShopFoodTypeCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategoryImage;
import com.tastyhouse.core.domain.shop.domain.model.Station;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityAssignmentResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityWithCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBannerImageResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeAssignmentResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhotoCategoryImageResult;

public interface ShopDetailRepository {

    List<Station> findAllStationsOrderByName();

    List<ShopFoodTypeCategoryResult> findAllActiveFoodTypeCategories();

    List<ShopAmenityCategoryResult> findAllActiveAmenityCategories();

    List<ShopAmenityCategory> findAllAmenityCategories();

    Optional<ShopAmenityCategory> findAmenityCategoryById(Long id);

    ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory);

    List<ShopFoodTypeCategory> findAllFoodTypeCategories();

    Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id);

    ShopFoodTypeCategory saveFoodTypeCategory(ShopFoodTypeCategory foodTypeCategory);

    List<ShopAmenityAssignmentResult> findAmenityAssignmentsByShopId(Long shopId);

    ShopAmenity saveAmenity(ShopAmenity amenity);

    void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId);

    List<ShopFoodTypeAssignmentResult> findFoodTypeAssignmentsByShopId(Long shopId);

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

    ShopClosedDay saveClosedDay(ShopClosedDay closedDay);

    void deleteClosedDayById(Long id);

    List<ShopAmenityWithCategoryResult> findAmenitiesWithCategoryByShopId(Long shopId);

    List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId);

    ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod);

    void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod);

    List<ShopBannerImageResult> findBannerImagesByShopId(Long shopId);

    List<ShopBannerImage> findBannerImageEntitiesByShopId(Long shopId);

    ShopBannerImage saveBannerImage(ShopBannerImage bannerImage);

    void deleteBannerImageById(Long id);

    List<ShopPhotoCategory> findPhotoCategoriesByShopId(Long shopId);

    Optional<ShopPhotoCategory> findPhotoCategoryById(Long id);

    ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory);

    void deletePhotoCategoryById(Long id);

    List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages();

    List<ShopPhotoCategoryImage> findPhotoCategoryImagesByCategoryId(Long shopPhotoCategoryId);

    Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id);

    ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage);

    void deletePhotoCategoryImageById(Long id);

    Optional<ShopOwnerMessageHistory> findLatestOwnerMessageByShopId(Long shopId);
}
