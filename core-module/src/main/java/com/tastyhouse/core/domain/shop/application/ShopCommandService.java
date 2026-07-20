package com.tastyhouse.core.domain.shop.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenity;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenityCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopBannerImage;
import com.tastyhouse.core.domain.shop.domain.model.ShopBookmark;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopChoice;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopFoodType;
import com.tastyhouse.core.domain.shop.domain.model.ShopFoodTypeCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategoryImage;
import com.tastyhouse.core.domain.shop.domain.model.Tag;
import com.tastyhouse.core.domain.shop.domain.repository.ShopBookmarkRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopChoiceRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.repository.StationRepository;
import com.tastyhouse.core.domain.shop.domain.repository.TagRepository;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopAmenityCategorySaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopBannerImageSaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopBreakTimeSaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopBusinessHourSaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopChoiceSaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopClosedDaySaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopCreateCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopFoodTypeCategorySaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopOrderMethodAssignCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopPhotoCategoryImageSaveCommand;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopUpdateCommand;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopCommandService {

    private final ShopQueryService shopQueryService;
    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopBookmarkRepository shopBookmarkRepository;
    private final ShopChoiceRepository shopChoiceRepository;
    private final TagRepository tagRepository;
    private final StationRepository stationRepository;

    public boolean toggleBookmark(Long shopId, MemberId memberId) {
        if (shopBookmarkRepository.existsByShopIdAndMemberId(shopId, memberId)) {
            shopBookmarkRepository.deleteByShopIdAndMemberId(shopId, memberId);
            return false;
        } else {
            shopQueryService.findShopById(ShopId.of(shopId));
            ShopBookmark shopBookmark = ShopBookmark.of(shopId, memberId);
            shopBookmarkRepository.save(shopBookmark);
            return true;
        }
    }

    public Shop createShop(ShopCreateCommand cmd) {
        validateStationExists(cmd.stationId());
        Shop shop = Shop.of(
            cmd.stationId(),
            cmd.name(),
            cmd.latitude(),
            cmd.longitude(),
            cmd.roadAddress(),
            cmd.lotAddress(),
            cmd.phoneNumber(),
            cmd.thumbnailImageFileId()
        );
        return shopRepository.save(shop);
    }

    public void updateShop(ShopId shopId, ShopUpdateCommand cmd) {
        validateStationExists(cmd.stationId());
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        shop.update(
            cmd.stationId(),
            cmd.name(),
            cmd.latitude(),
            cmd.longitude(),
            cmd.roadAddress(),
            cmd.lotAddress(),
            cmd.phoneNumber(),
            cmd.thumbnailImageFileId()
        );
        shopRepository.save(shop);
    }

    private void validateStationExists(Long stationId) {
        if (!stationRepository.existsById(stationId)) {
            throw new EntityNotFoundException(ErrorCode.STATION_NOT_FOUND);
        }
    }

    public void closeShop(ShopId shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        shop.close();
        shopRepository.save(shop);
    }

    public ShopBusinessHour createBusinessHour(Long shopId, ShopBusinessHourSaveCommand cmd) {
        ShopBusinessHour businessHour = ShopBusinessHour.of(shopId, cmd.dayType(), cmd.openTime(), cmd.closeTime(), cmd.isClosed());
        return shopDetailRepository.saveBusinessHour(businessHour);
    }

    public void updateBusinessHour(Long id, ShopBusinessHourSaveCommand cmd) {
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHourById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        businessHour.update(cmd.dayType(), cmd.openTime(), cmd.closeTime(), cmd.isClosed());
        shopDetailRepository.saveBusinessHour(businessHour);
    }

    public void deleteBusinessHour(Long id) {
        shopDetailRepository.deleteBusinessHourById(id);
    }

    public ShopBreakTime createBreakTime(Long shopId, ShopBreakTimeSaveCommand cmd) {
        ShopBreakTime breakTime = ShopBreakTime.of(shopId, cmd.dayType(), cmd.startTime(), cmd.endTime());
        return shopDetailRepository.saveBreakTime(breakTime);
    }

    public void updateBreakTime(Long id, ShopBreakTimeSaveCommand cmd) {
        ShopBreakTime breakTime = shopDetailRepository.findBreakTimeById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        breakTime.update(cmd.dayType(), cmd.startTime(), cmd.endTime());
        shopDetailRepository.saveBreakTime(breakTime);
    }

    public void deleteBreakTime(Long id) {
        shopDetailRepository.deleteBreakTimeById(id);
    }

    public ShopClosedDay createClosedDay(Long shopId, ShopClosedDaySaveCommand cmd) {
        ShopClosedDay closedDay = ShopClosedDay.of(shopId, cmd.closedDayType());
        return shopDetailRepository.saveClosedDay(closedDay);
    }

    public void deleteClosedDay(Long id) {
        shopDetailRepository.deleteClosedDayById(id);
    }

    public ShopAmenityCategory createAmenityCategory(ShopAmenityCategorySaveCommand cmd) {
        ShopAmenityCategory amenityCategory = ShopAmenityCategory.of(
            cmd.amenity(), cmd.displayName(), cmd.activeImageFileId(), cmd.inactiveImageFileId(), cmd.sort(), cmd.visible()
        );
        return shopDetailRepository.saveAmenityCategory(amenityCategory);
    }

    public void updateAmenityCategory(Long id, ShopAmenityCategorySaveCommand cmd) {
        ShopAmenityCategory amenityCategory = shopDetailRepository.findAmenityCategoryById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));
        amenityCategory.update(cmd.displayName(), cmd.activeImageFileId(), cmd.inactiveImageFileId(), cmd.sort(), cmd.visible());
        shopDetailRepository.saveAmenityCategory(amenityCategory);
    }

    public ShopFoodTypeCategory createFoodTypeCategory(ShopFoodTypeCategorySaveCommand cmd) {
        ShopFoodTypeCategory foodTypeCategory = ShopFoodTypeCategory.of(
            cmd.foodType(), cmd.displayName(), cmd.activeImageFileId(), cmd.inactiveImageFileId(), cmd.sort(), cmd.visible()
        );
        return shopDetailRepository.saveFoodTypeCategory(foodTypeCategory);
    }

    public void updateFoodTypeCategory(Long id, ShopFoodTypeCategorySaveCommand cmd) {
        ShopFoodTypeCategory foodTypeCategory = shopDetailRepository.findFoodTypeCategoryById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
        foodTypeCategory.update(cmd.displayName(), cmd.activeImageFileId(), cmd.inactiveImageFileId(), cmd.sort(), cmd.visible());
        shopDetailRepository.saveFoodTypeCategory(foodTypeCategory);
    }

    public ShopAmenity assignAmenity(Long shopId, Long amenityCategoryId) {
        shopDetailRepository.findAmenityCategoryById(amenityCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));
        ShopAmenity amenity = ShopAmenity.of(shopId, amenityCategoryId);
        return shopDetailRepository.saveAmenity(amenity);
    }

    public void unassignAmenity(Long shopId, Long amenityCategoryId) {
        shopDetailRepository.deleteAmenityByShopIdAndCategoryId(shopId, amenityCategoryId);
    }

    public ShopFoodType assignFoodType(Long shopId, Long foodTypeCategoryId) {
        shopDetailRepository.findFoodTypeCategoryById(foodTypeCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
        ShopFoodType foodType = ShopFoodType.of(shopId, foodTypeCategoryId);
        return shopDetailRepository.saveFoodType(foodType);
    }

    public void unassignFoodType(Long shopId, Long foodTypeCategoryId) {
        shopDetailRepository.deleteFoodTypeByShopIdAndCategoryId(shopId, foodTypeCategoryId);
    }

    public Tag createTag(String tagName) {
        Tag tag = Tag.of(tagName);
        return tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    public ShopOrderMethod assignOrderMethod(Long shopId, ShopOrderMethodAssignCommand cmd) {
        ShopOrderMethod orderMethod = ShopOrderMethod.of(shopId, cmd.orderMethod());
        return shopDetailRepository.saveOrderMethod(orderMethod);
    }

    public void unassignOrderMethod(Long shopId, ShopOrderMethodAssignCommand cmd) {
        shopDetailRepository.deleteOrderMethodByShopIdAndOrderMethod(shopId, cmd.orderMethod());
    }

    public ShopBannerImage createBannerImage(Long shopId, ShopBannerImageSaveCommand cmd) {
        ShopBannerImage bannerImage = ShopBannerImage.of(shopId, cmd.imageFileId(), cmd.sort());
        return shopDetailRepository.saveBannerImage(bannerImage);
    }

    public void deleteBannerImage(Long id) {
        shopDetailRepository.deleteBannerImageById(id);
    }

    public ShopPhotoCategory createPhotoCategory(Long shopId, String name) {
        ShopPhotoCategory photoCategory = ShopPhotoCategory.of(shopId, name);
        return shopDetailRepository.savePhotoCategory(photoCategory);
    }

    public void updatePhotoCategory(Long id, String name) {
        ShopPhotoCategory photoCategory = shopDetailRepository.findPhotoCategoryById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_PHOTO_CATEGORY_NOT_FOUND));
        photoCategory.update(name);
        shopDetailRepository.savePhotoCategory(photoCategory);
    }

    public void deletePhotoCategory(Long id) {
        shopDetailRepository.deletePhotoCategoryById(id);
    }

    public ShopPhotoCategoryImage createPhotoCategoryImage(Long categoryId, ShopPhotoCategoryImageSaveCommand cmd) {
        ShopPhotoCategoryImage image = ShopPhotoCategoryImage.of(categoryId, cmd.imageFileId(), cmd.sort(), cmd.visible());
        return shopDetailRepository.savePhotoCategoryImage(image);
    }

    public void updatePhotoCategoryImage(Long id, ShopPhotoCategoryImageSaveCommand cmd) {
        ShopPhotoCategoryImage image = shopDetailRepository.findPhotoCategoryImageById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_PHOTO_CATEGORY_IMAGE_NOT_FOUND));
        image.update(cmd.imageFileId(), cmd.sort(), cmd.visible());
        shopDetailRepository.savePhotoCategoryImage(image);
    }

    public void deletePhotoCategoryImage(Long id) {
        shopDetailRepository.deletePhotoCategoryImageById(id);
    }

    public ShopChoice createShopChoice(Long shopId, ShopChoiceSaveCommand cmd) {
        ShopChoice shopChoice = ShopChoice.of(shopId, cmd.title(), cmd.content());
        return shopChoiceRepository.save(shopChoice);
    }

    public void updateShopChoice(Long id, ShopChoiceSaveCommand cmd) {
        ShopChoice shopChoice = shopChoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_CHOICE_NOT_FOUND));
        shopChoice.update(cmd.title(), cmd.content());
        shopChoiceRepository.save(shopChoice);
    }

    public void deleteShopChoice(Long id) {
        shopChoiceRepository.deleteById(id);
    }
}
