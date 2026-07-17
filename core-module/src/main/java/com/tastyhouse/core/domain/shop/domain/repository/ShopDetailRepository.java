package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.Station;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityWithCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBannerImageResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhotoCategoryImageResult;

public interface ShopDetailRepository {

    List<Station> findAllStationsOrderByName();

    List<ShopFoodTypeCategoryResult> findAllActiveFoodTypeCategories();

    List<ShopAmenityCategoryResult> findAllActiveAmenityCategories();

    List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId);

    List<ShopBreakTime> findBreakTimesByShopId(Long shopId);

    List<ShopClosedDay> findClosedDaysByShopId(Long shopId);

    List<ShopAmenityWithCategoryResult> findAmenitiesWithCategoryByShopId(Long shopId);

    List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId);

    List<ShopBannerImageResult> findBannerImagesByShopId(Long shopId);

    List<ShopPhotoCategory> findPhotoCategoriesByShopId(Long shopId);

    List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages();

    Optional<ShopOwnerMessageHistory> findLatestOwnerMessageByShopId(Long shopId);
}
