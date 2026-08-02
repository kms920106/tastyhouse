package com.tastyhouse.adminapi.shop;

import java.math.BigDecimal;
import java.time.LocalTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.shop.domain.model.Amenity;
import com.tastyhouse.domain.shop.domain.model.ClosedDayType;
import com.tastyhouse.domain.shop.domain.model.DayType;
import com.tastyhouse.domain.shop.domain.model.FoodType;
import com.tastyhouse.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.domain.shop.domain.model.Shop;
import com.tastyhouse.domain.shop.domain.model.ShopAmenity;
import com.tastyhouse.domain.shop.domain.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.domain.model.ShopBannerImage;
import com.tastyhouse.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.domain.model.ShopChoice;
import com.tastyhouse.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.domain.shop.domain.model.ShopFoodType;
import com.tastyhouse.domain.shop.domain.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.domain.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.domain.model.Tag;
import com.tastyhouse.domain.shop.domain.repository.ShopChoiceRepository;
import com.tastyhouse.domain.shop.domain.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.domain.repository.TagRepository;
import com.tastyhouse.domain.shop.domain.service.ShopBusinessHourService;
import com.tastyhouse.domain.shop.domain.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.domain.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.domain.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.shop.domain.vo.ShopPhotoCategoryId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * admin용 가게 관리 변경 서비스(CQRS command 측).
 *
 * <p>가게 생성·수정·폐업과 노출정지 차단, 영업시간·휴게시간·정기휴무 규격 검증 등 크로스 애그리거트
 * 불변식은 도메인 서비스({@link ShopLifecycleService}·{@link ShopBusinessHourService})가 담당하고,
 * 이 서비스는 트랜잭션 경계와 경계 타입 승격(Long → VO, String → core enum)을 담당한다.
 *
 * <p>카테고리·배정·배너·사진·태그·에디터 추천은 단일 애그리거트 연산이라 도메인 서비스로 하강하지 않고
 * write 포트로 직접 다룬다. 도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로
 * {@code save}를 호출한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ShopCommandService {

    private final ShopLifecycleService shopLifecycleService;
    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopChoiceRepository shopChoiceRepository;
    private final TagRepository tagRepository;

    public Long createShop(
        Long ceoId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        Shop shop = shopLifecycleService.createShop(
            ceoId, stationId, name, latitude, longitude, roadAddress, lotAddress, phoneNumber, thumbnailImageFileId
        );
        return shop.getId();
    }

    public void updateShop(
        Long id,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        ShopId shopId = ShopId.of(id);
        shopLifecycleService.updateShop(
            shopId, stationId, name, latitude, longitude, roadAddress, lotAddress, phoneNumber, thumbnailImageFileId
        );
    }

    public void closeShop(Long id) {
        ShopId shopId = ShopId.of(id);
        shopLifecycleService.closeShop(shopId);
    }

    public Long createBusinessHour(
        Long id,
        String dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        ShopBusinessHour businessHour = shopBusinessHourService.createBusinessHour(
            id, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours
        );
        return businessHour.getId();
    }

    public void updateBusinessHour(
        Long businessHourId,
        String dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        shopBusinessHourService.updateBusinessHour(
            businessHourId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours
        );
    }

    public void deleteBusinessHour(Long businessHourId) {
        shopBusinessHourService.deleteBusinessHour(businessHourId);
    }

    public Long createBreakTime(Long id, String dayType, LocalTime startTime, LocalTime endTime) {
        ShopBreakTime breakTime = shopBusinessHourService.createBreakTime(
            id, DayType.from(dayType), startTime, endTime
        );
        return breakTime.getId();
    }

    public void updateBreakTime(Long breakTimeId, String dayType, LocalTime startTime, LocalTime endTime) {
        shopBusinessHourService.updateBreakTime(breakTimeId, DayType.from(dayType), startTime, endTime);
    }

    public void deleteBreakTime(Long breakTimeId) {
        shopBusinessHourService.deleteBreakTime(breakTimeId);
    }

    public Long createClosedDay(Long id, String closedDayType) {
        ShopClosedDay closedDay = shopBusinessHourService.createClosedDay(id, ClosedDayType.from(closedDayType));
        return closedDay.getId();
    }

    public void deleteClosedDay(Long closedDayId) {
        shopBusinessHourService.deleteClosedDay(closedDayId);
    }

    public Long createAmenityCategory(
        String amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        Boolean visible
    ) {
        ShopAmenityCategory amenityCategory = ShopAmenityCategory.of(
            Amenity.from(amenity),
            displayName,
            UploadedFileId.of(activeImageFileId),
            UploadedFileId.of(inactiveImageFileId),
            sort,
            visible
        );
        return shopDetailRepository.saveAmenityCategory(amenityCategory).getId();
    }

    public void updateAmenityCategory(
        Long categoryId,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        Boolean visible
    ) {
        ShopAmenityCategory amenityCategory = shopDetailRepository.findAmenityCategoryById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));
        amenityCategory.update(
            displayName,
            UploadedFileId.of(activeImageFileId),
            UploadedFileId.of(inactiveImageFileId),
            sort,
            visible
        );
        shopDetailRepository.saveAmenityCategory(amenityCategory);
    }

    public Long createFoodTypeCategory(
        String foodType,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        Boolean visible
    ) {
        ShopFoodTypeCategory foodTypeCategory = ShopFoodTypeCategory.of(
            FoodType.from(foodType),
            displayName,
            UploadedFileId.of(activeImageFileId),
            UploadedFileId.of(inactiveImageFileId),
            sort,
            visible
        );
        return shopDetailRepository.saveFoodTypeCategory(foodTypeCategory).getId();
    }

    public void updateFoodTypeCategory(
        Long categoryId,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        Boolean visible
    ) {
        ShopFoodTypeCategory foodTypeCategory = shopDetailRepository.findFoodTypeCategoryById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
        foodTypeCategory.update(
            displayName,
            UploadedFileId.of(activeImageFileId),
            UploadedFileId.of(inactiveImageFileId),
            sort,
            visible
        );
        shopDetailRepository.saveFoodTypeCategory(foodTypeCategory);
    }

    public Long assignAmenity(Long id, Long amenityCategoryId) {
        shopDetailRepository.findAmenityCategoryById(amenityCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));
        ShopAmenity amenity = shopDetailRepository.saveAmenity(ShopAmenity.of(ShopId.of(id), ShopAmenityCategoryId.of(amenityCategoryId)));
        return amenity.getId();
    }

    public void unassignAmenity(Long id, Long amenityCategoryId) {
        shopDetailRepository.deleteAmenityByShopIdAndCategoryId(id, amenityCategoryId);
    }

    public Long assignFoodType(Long id, Long foodTypeCategoryId) {
        shopDetailRepository.findFoodTypeCategoryById(foodTypeCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
        ShopFoodType foodType = shopDetailRepository.saveFoodType(ShopFoodType.of(ShopId.of(id), ShopFoodTypeCategoryId.of(foodTypeCategoryId)));
        return foodType.getId();
    }

    public void unassignFoodType(Long id, Long foodTypeCategoryId) {
        shopDetailRepository.deleteFoodTypeByShopIdAndCategoryId(id, foodTypeCategoryId);
    }

    public Long createTag(String tagName) {
        Tag tag = tagRepository.save(Tag.of(tagName));
        return tag.getId();
    }

    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    public Long assignOrderMethod(Long id, String orderMethod) {
        ShopOrderMethod saved = shopDetailRepository.saveOrderMethod(
            ShopOrderMethod.of(ShopId.of(id), OrderMethod.from(orderMethod))
        );
        return saved.getId();
    }

    public void unassignOrderMethod(Long id, String orderMethod) {
        shopDetailRepository.deleteOrderMethodByShopIdAndOrderMethod(id, OrderMethod.from(orderMethod));
    }

    public Long createBannerImage(Long id, Long imageFileId, Integer sort) {
        ShopBannerImage bannerImage = shopDetailRepository.saveBannerImage(
            ShopBannerImage.of(ShopId.of(id), UploadedFileId.of(imageFileId), sort)
        );
        return bannerImage.getId();
    }

    public void deleteBannerImage(Long bannerImageId) {
        shopDetailRepository.deleteBannerImageById(bannerImageId);
    }

    public Long createPhotoCategory(Long id, String name) {
        ShopPhotoCategory photoCategory = shopDetailRepository.savePhotoCategory(ShopPhotoCategory.of(ShopId.of(id), name));
        return photoCategory.getId();
    }

    public void updatePhotoCategory(Long categoryId, String name) {
        ShopPhotoCategory photoCategory = shopDetailRepository.findPhotoCategoryById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_PHOTO_CATEGORY_NOT_FOUND));
        photoCategory.update(name);
        shopDetailRepository.savePhotoCategory(photoCategory);
    }

    public void deletePhotoCategory(Long categoryId) {
        shopDetailRepository.deletePhotoCategoryById(categoryId);
    }

    public Long createPhotoCategoryImage(Long categoryId, Long imageFileId, Integer sort, Boolean visible) {
        ShopPhotoCategoryImage image = shopDetailRepository.savePhotoCategoryImage(
            ShopPhotoCategoryImage.of(
                ShopPhotoCategoryId.of(categoryId),
                UploadedFileId.of(imageFileId),
                sort,
                visible
            )
        );
        return image.getId();
    }

    public void updatePhotoCategoryImage(Long imageId, Long imageFileId, Integer sort, Boolean visible) {
        ShopPhotoCategoryImage image = shopDetailRepository.findPhotoCategoryImageById(imageId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_PHOTO_CATEGORY_IMAGE_NOT_FOUND));
        image.update(UploadedFileId.of(imageFileId), sort, visible);
        shopDetailRepository.savePhotoCategoryImage(image);
    }

    public void deletePhotoCategoryImage(Long imageId) {
        shopDetailRepository.deletePhotoCategoryImageById(imageId);
    }

    public Long createShopChoice(Long shopId, String title, String content) {
        ShopChoice shopChoice = shopChoiceRepository.save(ShopChoice.of(ShopId.of(shopId), title, content));
        return shopChoice.getId();
    }

    public void updateShopChoice(Long id, String title, String content) {
        ShopChoice shopChoice = shopChoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_CHOICE_NOT_FOUND));
        shopChoice.update(title, content);
        shopChoiceRepository.save(shopChoice);
    }

    public void deleteShopChoice(Long id) {
        shopChoiceRepository.deleteById(id);
    }
}
