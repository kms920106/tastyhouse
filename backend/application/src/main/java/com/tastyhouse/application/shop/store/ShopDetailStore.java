package com.tastyhouse.application.shop.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopDetailStatePort;
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

public class ShopDetailStore implements ShopDetailRepository {
    private final ShopDetailStatePort shopDetailStatePort;

    public ShopDetailStore(ShopDetailStatePort shopDetailStatePort) {
        this.shopDetailStatePort = shopDetailStatePort;
    }

    @Override
    public Optional<ShopAmenityCategory> findAmenityCategoryById(Long id) {
        return shopDetailStatePort.findAmenityCategoryById(id).map(ShopAmenityCategoryStateMapper::toDomain);
    }

    @Override
    public ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory) {
        return ShopAmenityCategoryStateMapper.toDomain(shopDetailStatePort.saveAmenityCategory(ShopAmenityCategoryStateMapper.toState(amenityCategory)));
    }

    @Override
    public Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id) {
        return shopDetailStatePort.findFoodTypeCategoryById(id).map(ShopFoodTypeCategoryStateMapper::toDomain);
    }

    @Override
    public ShopFoodTypeCategory saveFoodTypeCategory(ShopFoodTypeCategory foodTypeCategory) {
        return ShopFoodTypeCategoryStateMapper.toDomain(shopDetailStatePort.saveFoodTypeCategory(ShopFoodTypeCategoryStateMapper.toState(foodTypeCategory)));
    }

    @Override
    public ShopAmenity saveAmenity(ShopAmenity amenity) {
        return ShopAmenityStateMapper.toDomain(shopDetailStatePort.saveAmenity(ShopAmenityStateMapper.toState(amenity)));
    }

    @Override
    public void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId) {
        shopDetailStatePort.deleteAmenityByShopIdAndCategoryId(shopId, shopAmenityCategoryId);
    }

    @Override
    public ShopFoodType saveFoodType(ShopFoodType foodType) {
        return ShopFoodTypeStateMapper.toDomain(shopDetailStatePort.saveFoodType(ShopFoodTypeStateMapper.toState(foodType)));
    }

    @Override
    public void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId) {
        shopDetailStatePort.deleteFoodTypeByShopIdAndCategoryId(shopId, shopFoodTypeCategoryId);
    }

    @Override
    public List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId) {
        return shopDetailStatePort.findBusinessHoursByShopId(shopId).stream()
            .map(ShopBusinessHourStateMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopBusinessHour> findBusinessHourById(Long id) {
        return shopDetailStatePort.findBusinessHourById(id).map(ShopBusinessHourStateMapper::toDomain);
    }

    @Override
    public ShopBusinessHour saveBusinessHour(ShopBusinessHour businessHour) {
        return ShopBusinessHourStateMapper.toDomain(shopDetailStatePort.saveBusinessHour(ShopBusinessHourStateMapper.toState(businessHour)));
    }

    @Override
    public void deleteBusinessHourById(Long id) {
        shopDetailStatePort.deleteBusinessHourById(id);
    }

    @Override
    public List<ShopBreakTime> findBreakTimesByShopId(Long shopId) {
        return shopDetailStatePort.findBreakTimesByShopId(shopId).stream()
            .map(ShopBreakTimeStateMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopBreakTime> findBreakTimeById(Long id) {
        return shopDetailStatePort.findBreakTimeById(id).map(ShopBreakTimeStateMapper::toDomain);
    }

    @Override
    public ShopBreakTime saveBreakTime(ShopBreakTime breakTime) {
        return ShopBreakTimeStateMapper.toDomain(shopDetailStatePort.saveBreakTime(ShopBreakTimeStateMapper.toState(breakTime)));
    }

    @Override
    public void deleteBreakTimeById(Long id) {
        shopDetailStatePort.deleteBreakTimeById(id);
    }

    @Override
    public List<ShopClosedDay> findClosedDaysByShopId(Long shopId) {
        return shopDetailStatePort.findClosedDaysByShopId(shopId).stream()
            .map(ShopClosedDayStateMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ShopClosedDay> findClosedDayById(Long id) {
        return shopDetailStatePort.findClosedDayById(id).map(ShopClosedDayStateMapper::toDomain);
    }

    @Override
    public ShopClosedDay saveClosedDay(ShopClosedDay closedDay) {
        return ShopClosedDayStateMapper.toDomain(shopDetailStatePort.saveClosedDay(ShopClosedDayStateMapper.toState(closedDay)));
    }

    @Override
    public void deleteClosedDayById(Long id) {
        shopDetailStatePort.deleteClosedDayById(id);
    }

    @Override
    public List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId) {
        return shopDetailStatePort.findOrderMethodsByShopId(shopId).stream()
            .map(ShopOrderMethodStateMapper::toDomain)
            .toList();
    }

    @Override
    public ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod) {
        return ShopOrderMethodStateMapper.toDomain(shopDetailStatePort.saveOrderMethod(ShopOrderMethodStateMapper.toState(orderMethod)));
    }

    @Override
    public void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod) {
        shopDetailStatePort.deleteOrderMethodByShopIdAndOrderMethod(shopId, orderMethod == null ? null : orderMethod.name());
    }

    @Override
    public ShopBannerImage saveBannerImage(ShopBannerImage bannerImage) {
        return ShopBannerImageStateMapper.toDomain(shopDetailStatePort.saveBannerImage(ShopBannerImageStateMapper.toState(bannerImage)));
    }

    @Override
    public void deleteBannerImageById(Long id) {
        shopDetailStatePort.deleteBannerImageById(id);
    }

    @Override
    public Optional<ShopPhotoCategory> findPhotoCategoryById(Long id) {
        return shopDetailStatePort.findPhotoCategoryById(id).map(ShopPhotoCategoryStateMapper::toDomain);
    }

    @Override
    public ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory) {
        return ShopPhotoCategoryStateMapper.toDomain(shopDetailStatePort.savePhotoCategory(ShopPhotoCategoryStateMapper.toState(photoCategory)));
    }

    @Override
    public void deletePhotoCategoryById(Long id) {
        shopDetailStatePort.deletePhotoCategoryById(id);
    }

    @Override
    public Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id) {
        return shopDetailStatePort.findPhotoCategoryImageById(id).map(ShopPhotoCategoryImageStateMapper::toDomain);
    }

    @Override
    public ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage) {
        return ShopPhotoCategoryImageStateMapper.toDomain(shopDetailStatePort.savePhotoCategoryImage(ShopPhotoCategoryImageStateMapper.toState(photoCategoryImage)));
    }

    @Override
    public void deletePhotoCategoryImageById(Long id) {
        shopDetailStatePort.deletePhotoCategoryImageById(id);
    }

    @Override
    public void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory) {
        shopDetailStatePort.saveOwnerMessage(ShopOwnerMessageHistoryStateMapper.toState(ownerMessageHistory));
    }

    @Override
    public Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId) {
        return shopDetailStatePort.findLatestOwnerMessage(shopId).map(ShopOwnerMessageHistoryStateMapper::toDomain);
    }
}
