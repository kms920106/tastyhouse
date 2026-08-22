package com.tastyhouse.adminapi.shop;

import java.math.BigDecimal;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.ClosedDayType;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.domain.shop.repository.ShopChoiceRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.TagRepository;
import com.tastyhouse.domain.shop.service.ShopBusinessHourService;
import com.tastyhouse.domain.shop.service.ShopCeoAssignmentService;
import com.tastyhouse.domain.shop.service.ShopConvenienceInfoService;
import com.tastyhouse.domain.shop.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopPhotoCategoryId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

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
public class ShopCommandService {

    private final ShopLifecycleService shopLifecycleService;
    private final ShopBusinessHourService shopBusinessHourService;
    private final ShopConvenienceInfoService shopConvenienceInfoService;
    private final ShopCeoAssignmentService shopCeoAssignmentService;
    private final ShopDetailRepository shopDetailRepository;
    private final ShopChoiceRepository shopChoiceRepository;
    private final TagRepository tagRepository;

    public ShopCommandService(
        ShopLifecycleService shopLifecycleService,
        ShopBusinessHourService shopBusinessHourService,
        ShopConvenienceInfoService shopConvenienceInfoService,
        ShopCeoAssignmentService shopCeoAssignmentService,
        ShopDetailRepository shopDetailRepository,
        ShopChoiceRepository shopChoiceRepository,
        TagRepository tagRepository
    ) {
        this.shopLifecycleService = shopLifecycleService;
        this.shopBusinessHourService = shopBusinessHourService;
        this.shopConvenienceInfoService = shopConvenienceInfoService;
        this.shopCeoAssignmentService = shopCeoAssignmentService;
        this.shopDetailRepository = shopDetailRepository;
        this.shopChoiceRepository = shopChoiceRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * 가게를 등록한다.
     *
     * <p>{@code ceoId}를 함께 지정하면 접근권한 부여 이력({@code GRANT})이 남으므로, 조치한 관리자
     * 식별자({@code adminId})를 첫 파라미터로 받는다. 요청·응답 계약은 변하지 않는다 —
     * {@code adminId}는 본문이 아니라 인증 주체에서 온다.
     */
    public Long createShop(
        Long adminId,
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
            adminId, ceoId, stationId, name, latitude, longitude, roadAddress, lotAddress, phoneNumber,
            thumbnailImageFileId
        );
        return shop.getId();
    }

    /**
     * 가게에 담당 점주를 배정한다. 다른 점주가 이미 배정돼 있으면 말소 후 부여로 2행이 남는다.
     */
    public void assignCeo(Long adminId, Long id, Long ceoId) {
        ShopId shopId = ShopId.of(id);
        CeoId targetCeoId = CeoId.of(ceoId);
        shopCeoAssignmentService.assign(shopId, targetCeoId, adminId);
    }

    /**
     * 가게의 담당 점주 배정을 해제한다.
     */
    public void revokeCeo(Long adminId, Long id) {
        ShopId shopId = ShopId.of(id);
        shopCeoAssignmentService.revoke(shopId, adminId);
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

    /**
     * 일회용컵 보증금제 대상 사업자 지정/해제를 토글한다.
     *
     * <p><b>admin 전용인 이유</b>: 이것은 점주의 영업 설정이 아니라 환경부·자원순환보증금관리센터가
     * 정하는 외부 규제 사실이다. 점주가 스스로 켤 수 있으면 대상이 아닌 가게가 보증금을 부과하게 된다.
     */
    public void changeCupDepositEnabled(Long id, boolean enabled) {
        ShopId shopId = ShopId.of(id);
        shopLifecycleService.changeCupDepositEnabled(shopId, enabled);
    }

    public Long createBusinessHour(
        Long adminId,
        Long id,
        String dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopBusinessHour businessHour = shopBusinessHourService.createBusinessHour(
            id, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
        return businessHour.getId();
    }

    public void updateBusinessHour(
        Long adminId,
        Long businessHourId,
        String dayType,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isClosed,
        Boolean is24Hours
    ) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.updateBusinessHour(
            businessHourId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
    }

    public void deleteBusinessHour(Long adminId, Long businessHourId) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteBusinessHour(businessHourId, actor);
    }

    public Long createBreakTime(Long adminId, Long id, String dayType, LocalTime startTime, LocalTime endTime) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopBreakTime breakTime = shopBusinessHourService.createBreakTime(
            id, DayType.from(dayType), startTime, endTime, actor
        );
        return breakTime.getId();
    }

    public void updateBreakTime(Long adminId, Long breakTimeId, String dayType, LocalTime startTime, LocalTime endTime) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.updateBreakTime(breakTimeId, DayType.from(dayType), startTime, endTime, actor);
    }

    public void deleteBreakTime(Long adminId, Long breakTimeId) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteBreakTime(breakTimeId, actor);
    }

    public Long createClosedDay(Long adminId, Long id, String closedDayType) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopClosedDay closedDay = shopBusinessHourService.createClosedDay(id, ClosedDayType.from(closedDayType), actor);
        return closedDay.getId();
    }

    public void deleteClosedDay(Long adminId, Long closedDayId) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteClosedDay(closedDayId, actor);
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
        foodTypeCategory.update(
            displayName,
            UploadedFileId.of(activeImageFileId),
            UploadedFileId.of(inactiveImageFileId),
            sort,
            visible
        );
        shopDetailRepository.saveFoodTypeCategory(foodTypeCategory);
    }

    /**
     * 가게에 편의시설을 배정한다. 카테고리 존재 검증과 변경이력({@code AMENITY}) 기록은 도메인 서비스가
     * 담당한다.
     */
    public Long assignAmenity(Long adminId, Long id, Long amenityCategoryId) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        return shopConvenienceInfoService.assignAmenity(id, amenityCategoryId, actor);
    }

    /**
     * 가게에 배정된 편의시설을 해제한다. 변경이력({@code AMENITY})은 도메인 서비스가 남긴다.
     */
    public void unassignAmenity(Long adminId, Long id, Long amenityCategoryId) {
        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopConvenienceInfoService.unassignAmenity(id, amenityCategoryId, actor);
    }

    public Long assignFoodType(Long id, Long foodTypeCategoryId) {
        shopDetailRepository.findFoodTypeCategoryById(foodTypeCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_PHOTO_CATEGORY_NOT_FOUND));
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_PHOTO_CATEGORY_IMAGE_NOT_FOUND));
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_CHOICE_NOT_FOUND));
        shopChoice.update(title, content);
        shopChoiceRepository.save(shopChoice);
    }

    public void deleteShopChoice(Long id) {
        shopChoiceRepository.deleteById(id);
    }
}
