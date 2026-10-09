package com.tastyhouse.application.shop.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;

public interface ShopDetailLoadPort {

    Optional<ShopAmenityCategory> findAmenityCategoryById(Long id);

    Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id);

    List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId);

    Optional<ShopBusinessHour> findBusinessHourById(Long id);

    List<ShopBreakTime> findBreakTimesByShopId(Long shopId);

    Optional<ShopBreakTime> findBreakTimeById(Long id);

    List<ShopClosedDay> findClosedDaysByShopId(Long shopId);

    Optional<ShopClosedDay> findClosedDayById(Long id);

    List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId);

    Optional<ShopPhotoCategory> findPhotoCategoryById(Long id);

    Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id);

    Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId);
}
