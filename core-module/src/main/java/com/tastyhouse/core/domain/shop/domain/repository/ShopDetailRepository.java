package com.tastyhouse.core.domain.shop.domain.repository;

import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityWithCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBannerImageDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhotoCategoryImageDto;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenity;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.Station;

import java.util.List;
import java.util.Optional;

public interface ShopDetailRepository {

    List<Station> findAllStationsOrderByName();

    List<ShopFoodTypeCategoryDto> findAllActiveFoodTypeCategories();

    List<ShopAmenityCategoryDto> findAllActiveAmenityCategories();

    List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId);

    List<ShopBreakTime> findBreakTimesByShopId(Long shopId);

    List<ShopClosedDay> findClosedDaysByShopId(Long shopId);

    List<ShopAmenity> findAmenitiesByShopId(Long shopId);

    List<ShopAmenityWithCategoryDto> findAmenitiesWithCategoryByShopId(Long shopId);

    List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId);

    List<ShopBannerImageDto> findBannerImagesByShopId(Long shopId);

    List<ShopPhotoCategory> findPhotoCategoriesByShopId(Long shopId);

    List<ShopPhotoCategoryImageDto> findAllPhotoCategoryImages();

    Optional<ShopOwnerMessageHistory> findLatestOwnerMessageByShopId(Long shopId);
}
