package com.tastyhouse.core.domain.shop.application;

import java.time.LocalTime;

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
import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
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
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopCommandService {

    private static final int MAX_REGULAR_CLOSED_DAY_COUNT = 15;
    private static final int SHOP_INTRODUCTION_MAX_LENGTH = 500;

    private final ShopQueryService shopQueryService;
    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopBookmarkRepository shopBookmarkRepository;
    private final ShopChoiceRepository shopChoiceRepository;
    private final TagRepository tagRepository;
    private final StationRepository stationRepository;
    private final ShopImageChangeQueryService shopImageChangeQueryService;
    private final ProhibitedWordValidator prohibitedWordValidator;

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
        shop.assignCeo(cmd.ceoId());
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
        validateBusinessHour(cmd);
        ShopBusinessHour businessHour = ShopBusinessHour.of(
            shopId, cmd.dayType(), cmd.openTime(), cmd.closeTime(), cmd.isClosed(), cmd.is24Hours()
        );
        return shopDetailRepository.saveBusinessHour(businessHour);
    }

    public void updateBusinessHour(Long id, ShopBusinessHourSaveCommand cmd) {
        validateBusinessHour(cmd);
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHourById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_BUSINESS_HOUR_NOT_FOUND));
        businessHour.update(cmd.dayType(), cmd.openTime(), cmd.closeTime(), cmd.isClosed(), cmd.is24Hours());
        shopDetailRepository.saveBusinessHour(businessHour);
    }

    public void deleteBusinessHour(Long id) {
        shopDetailRepository.deleteBusinessHourById(id);
    }

    public ShopBreakTime createBreakTime(Long shopId, ShopBreakTimeSaveCommand cmd) {
        validateBreakTimeWithinBusinessHours(shopId, cmd);
        ShopBreakTime breakTime = ShopBreakTime.of(shopId, cmd.dayType(), cmd.startTime(), cmd.endTime());
        return shopDetailRepository.saveBreakTime(breakTime);
    }

    public void updateBreakTime(Long id, ShopBreakTimeSaveCommand cmd) {
        ShopBreakTime breakTime = shopDetailRepository.findBreakTimeById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_BREAK_TIME_NOT_FOUND));
        validateBreakTimeWithinBusinessHours(breakTime.getShopId(), cmd);
        breakTime.update(cmd.dayType(), cmd.startTime(), cmd.endTime());
        shopDetailRepository.saveBreakTime(breakTime);
    }

    /**
     * 영업시간 PDF 규격을 검증한다: 휴무/24시간이면 시간 검증 생략, 그 외에는 5분 단위·최소 1시간~최대 23시간 55분.
     * 자정 넘김(종료 &lt; 시작)은 허용하며 다음날로 넘어간 것으로 계산한다.
     */
    private void validateBusinessHour(ShopBusinessHourSaveCommand cmd) {
        if (Boolean.TRUE.equals(cmd.isClosed()) || Boolean.TRUE.equals(cmd.is24Hours())) {
            return;
        }
        LocalTime open = cmd.openTime();
        LocalTime close = cmd.closeTime();
        if (open == null || close == null) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
        }
        if (isNotFiveMinuteUnit(open) || isNotFiveMinuteUnit(close)) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_UNIT);
        }
        long durationMinutes = minutesBetween(open, close);
        if (durationMinutes < 60 || durationMinutes > 23 * 60 + 55) {
            throw new BusinessException(ErrorCode.SHOP_BUSINESS_HOUR_INVALID_RANGE);
        }
    }

    /**
     * 휴게시간이 같은 요일 영업시간 범위 안에 있는지 검증한다(자정 넘김 반영). 영업시간과 완전히 동일하면 거부한다.
     */
    private void validateBreakTimeWithinBusinessHours(Long shopId, ShopBreakTimeSaveCommand cmd) {
        LocalTime breakStart = cmd.startTime();
        LocalTime breakEnd = cmd.endTime();
        if (breakStart == null || breakEnd == null) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
        ShopBusinessHour businessHour = shopDetailRepository.findBusinessHoursByShopId(shopId).stream()
            .filter(bh -> bh.getDayType() == cmd.dayType())
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS));
        if (Boolean.TRUE.equals(businessHour.getIsClosed())) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
        if (Boolean.TRUE.equals(businessHour.getIs24Hours())) {
            return; // 24시간 영업이면 어떤 휴게시간도 범위 내
        }
        LocalTime open = businessHour.getOpenTime();
        LocalTime close = businessHour.getCloseTime();
        if (open != null && close != null && open.equals(breakStart) && close.equals(breakEnd)) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_EQUALS_BUSINESS_HOURS);
        }
        if (isOutside(open, close, breakStart) || isOutside(open, close, breakEnd)) {
            throw new BusinessException(ErrorCode.SHOP_BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }
    }

    private boolean isNotFiveMinuteUnit(LocalTime time) {
        return time.getMinute() % 5 != 0 || time.getSecond() != 0 || time.getNano() != 0;
    }

    /**
     * open→close 경과 분. 자정 넘김(close ≤ open)이면 다음날로 넘어간 것으로 24시간을 더해 계산한다.
     */
    private long minutesBetween(LocalTime open, LocalTime close) {
        int openMin = open.getHour() * 60 + open.getMinute();
        int closeMin = close.getHour() * 60 + close.getMinute();
        int diff = closeMin - openMin;
        if (diff <= 0) {
            diff += 24 * 60;
        }
        return diff;
    }

    /**
     * target이 [open, close] 영업 구간 밖에 있는지 판정한다(자정 넘김 구간이면 두 조각으로 나눠 판정).
     */
    private boolean isOutside(LocalTime open, LocalTime close, LocalTime target) {
        if (open == null || close == null) {
            return true;
        }
        if (open.isBefore(close)) {
            return target.isBefore(open) || target.isAfter(close);
        }
        // 자정 넘김: open~24:00 또는 00:00~close
        return target.isBefore(open) && target.isAfter(close);
    }

    public void deleteBreakTime(Long id) {
        shopDetailRepository.deleteBreakTimeById(id);
    }

    public ShopClosedDay createClosedDay(Long shopId, ShopClosedDaySaveCommand cmd) {
        if (shopDetailRepository.findClosedDaysByShopId(shopId).size() >= MAX_REGULAR_CLOSED_DAY_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_REGULAR_CLOSED_DAY_LIMIT_EXCEEDED);
        }
        ShopClosedDay closedDay = ShopClosedDay.of(shopId, cmd.closedDayType());
        return shopDetailRepository.saveClosedDay(closedDay);
    }

    public void deleteClosedDay(Long id) {
        shopDetailRepository.deleteClosedDayById(id);
    }

    /**
     * 공휴일 휴무 여부를 설정한다.
     */
    public void updateHolidayClosure(ShopId shopId, boolean closedOnPublicHolidays) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        shop.updateHolidayClosure(closedOnPublicHolidays);
        shopRepository.save(shop);
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

    /**
     * 가게 노출 상태(노출정지)를 변경한다. 진행 중인 승인 요청이 있으면 상태 변경을 차단한다.
     */
    public void changeVisibility(ShopId shopId, boolean hidden) {
        if (shopImageChangeQueryService.existsPendingByShopId(shopId.value())) {
            throw new BusinessException(ErrorCode.SHOP_STATUS_CHANGE_BLOCKED_BY_PENDING_REQUEST);
        }
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (hidden) {
            shop.hide();
        } else {
            shop.show();
        }
        shopRepository.save(shop);
    }

    /**
     * 사장님 한마디(가게소개)를 새로 등록한다. 최대 500자 제한과 금칙어 검수를 통과해야 한다.
     */
    public void createOwnerMessage(Long shopId, String message) {
        if (message != null && message.length() > SHOP_INTRODUCTION_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_INTRODUCTION_TOO_LONG);
        }
        prohibitedWordValidator.validate(message);
        ShopOwnerMessageHistory ownerMessageHistory = ShopOwnerMessageHistory.of(shopId, message);
        shopDetailRepository.saveOwnerMessage(ownerMessageHistory);
    }
}
