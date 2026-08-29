package com.tastyhouse.adminapi.shop.application.service;

import com.tastyhouse.adminapi.shop.application.port.in.ShopAmenityAssignCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopAmenityAssignUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopAmenityCategoryCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopAmenityCategoryCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopAmenityCategoryUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopAmenityCategoryUpdateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopAmenityUnassignCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopAmenityUnassignUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBannerImageCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBannerImageCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBannerImageDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBannerImageDeleteUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBreakTimeCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBreakTimeCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBreakTimeDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBreakTimeDeleteUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBreakTimeUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBreakTimeUpdateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBusinessHourCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBusinessHourCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBusinessHourDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBusinessHourDeleteUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBusinessHourUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopBusinessHourUpdateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCeoAssignCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCeoAssignUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCeoRevokeCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCeoRevokeUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopChoiceCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopChoiceCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopChoiceDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopChoiceDeleteUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopChoiceUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopChoiceUpdateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCloseCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCloseUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopClosedDayCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopClosedDayCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopClosedDayDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopClosedDayDeleteUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCupDepositChangeCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopCupDepositChangeUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeAssignCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeAssignUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeCategoryCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeCategoryCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeCategoryUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeCategoryUpdateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeUnassignCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopFoodTypeUnassignUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopOrderMethodAssignCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopOrderMethodAssignUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopOrderMethodUnassignCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopOrderMethodUnassignUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryDeleteUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryImageCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryImageCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryImageDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryImageDeleteUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryImageUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryImageUpdateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopPhotoCategoryUpdateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopUpdateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopUpdateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.TagCreateCommand;
import com.tastyhouse.adminapi.shop.application.port.in.TagCreateUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.TagDeleteCommand;
import com.tastyhouse.adminapi.shop.application.port.in.TagDeleteUseCase;

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
public class ShopCommandService implements
    ShopCreateUseCase,
    ShopCeoAssignUseCase,
    ShopCeoRevokeUseCase,
    ShopUpdateUseCase,
    ShopCloseUseCase,
    ShopCupDepositChangeUseCase,
    ShopBusinessHourCreateUseCase,
    ShopBusinessHourUpdateUseCase,
    ShopBusinessHourDeleteUseCase,
    ShopBreakTimeCreateUseCase,
    ShopBreakTimeUpdateUseCase,
    ShopBreakTimeDeleteUseCase,
    ShopClosedDayCreateUseCase,
    ShopClosedDayDeleteUseCase,
    ShopAmenityCategoryCreateUseCase,
    ShopAmenityCategoryUpdateUseCase,
    ShopFoodTypeCategoryCreateUseCase,
    ShopFoodTypeCategoryUpdateUseCase,
    ShopAmenityAssignUseCase,
    ShopAmenityUnassignUseCase,
    ShopFoodTypeAssignUseCase,
    ShopFoodTypeUnassignUseCase,
    TagCreateUseCase,
    TagDeleteUseCase,
    ShopOrderMethodAssignUseCase,
    ShopOrderMethodUnassignUseCase,
    ShopBannerImageCreateUseCase,
    ShopBannerImageDeleteUseCase,
    ShopPhotoCategoryCreateUseCase,
    ShopPhotoCategoryUpdateUseCase,
    ShopPhotoCategoryDeleteUseCase,
    ShopPhotoCategoryImageCreateUseCase,
    ShopPhotoCategoryImageUpdateUseCase,
    ShopPhotoCategoryImageDeleteUseCase,
    ShopChoiceCreateUseCase,
    ShopChoiceUpdateUseCase,
    ShopChoiceDeleteUseCase {

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
    @Override
    public Long createShop(ShopCreateCommand command) {
        Long adminId = command.adminId();
        Long ceoId = command.ceoId();
        Long stationId = command.stationId();
        String name = command.name();
        BigDecimal latitude = command.latitude();
        BigDecimal longitude = command.longitude();
        String roadAddress = command.roadAddress();
        String lotAddress = command.lotAddress();
        String phoneNumber = command.phoneNumber();
        Long thumbnailImageFileId = command.thumbnailImageFileId();

        Shop shop = shopLifecycleService.createShop(
            adminId, ceoId, stationId, name, latitude, longitude, roadAddress, lotAddress, phoneNumber,
            thumbnailImageFileId
        );
        return shop.getId();
    }

    /**
     * 가게에 담당 점주를 배정한다. 다른 점주가 이미 배정돼 있으면 말소 후 부여로 2행이 남는다.
     */
    @Override
    public void assignCeo(ShopCeoAssignCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        Long ceoId = command.ceoId();

        ShopId shopId = ShopId.of(id);
        CeoId targetCeoId = CeoId.of(ceoId);
        shopCeoAssignmentService.assign(shopId, targetCeoId, adminId);
    }

    /**
     * 가게의 담당 점주 배정을 해제한다.
     */
    @Override
    public void revokeCeo(ShopCeoRevokeCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();

        ShopId shopId = ShopId.of(id);
        shopCeoAssignmentService.revoke(shopId, adminId);
    }

    @Override
    public void updateShop(ShopUpdateCommand command) {
        Long id = command.shopId();
        Long stationId = command.stationId();
        String name = command.name();
        BigDecimal latitude = command.latitude();
        BigDecimal longitude = command.longitude();
        String roadAddress = command.roadAddress();
        String lotAddress = command.lotAddress();
        String phoneNumber = command.phoneNumber();
        Long thumbnailImageFileId = command.thumbnailImageFileId();

        ShopId shopId = ShopId.of(id);
        shopLifecycleService.updateShop(
            shopId, stationId, name, latitude, longitude, roadAddress, lotAddress, phoneNumber, thumbnailImageFileId
        );
    }

    @Override
    public void closeShop(ShopCloseCommand command) {
        Long id = command.shopId();

        ShopId shopId = ShopId.of(id);
        shopLifecycleService.closeShop(shopId);
    }

    /**
     * 일회용컵 보증금제 대상 사업자 지정/해제를 토글한다.
     *
     * <p><b>admin 전용인 이유</b>: 이것은 점주의 영업 설정이 아니라 환경부·자원순환보증금관리센터가
     * 정하는 외부 규제 사실이다. 점주가 스스로 켤 수 있으면 대상이 아닌 가게가 보증금을 부과하게 된다.
     */
    @Override
    public void changeCupDepositEnabled(ShopCupDepositChangeCommand command) {
        Long id = command.shopId();
        boolean enabled = command.enabled();

        ShopId shopId = ShopId.of(id);
        shopLifecycleService.changeCupDepositEnabled(shopId, enabled);
    }

    @Override
    public Long createBusinessHour(ShopBusinessHourCreateCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        String dayType = command.dayType();
        LocalTime openTime = command.openTime();
        LocalTime closeTime = command.closeTime();
        Boolean isClosed = command.isClosed();
        Boolean is24Hours = command.is24Hours();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopBusinessHour businessHour = shopBusinessHourService.createBusinessHour(
            id, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
        return businessHour.getId();
    }

    @Override
    public void updateBusinessHour(ShopBusinessHourUpdateCommand command) {
        Long adminId = command.adminId();
        Long businessHourId = command.businessHourId();
        String dayType = command.dayType();
        LocalTime openTime = command.openTime();
        LocalTime closeTime = command.closeTime();
        Boolean isClosed = command.isClosed();
        Boolean is24Hours = command.is24Hours();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.updateBusinessHour(
            businessHourId, DayType.from(dayType), openTime, closeTime, isClosed, is24Hours, actor
        );
    }

    @Override
    public void deleteBusinessHour(ShopBusinessHourDeleteCommand command) {
        Long adminId = command.adminId();
        Long businessHourId = command.businessHourId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteBusinessHour(businessHourId, actor);
    }

    @Override
    public Long createBreakTime(ShopBreakTimeCreateCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        String dayType = command.dayType();
        LocalTime startTime = command.startTime();
        LocalTime endTime = command.endTime();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopBreakTime breakTime = shopBusinessHourService.createBreakTime(
            id, DayType.from(dayType), startTime, endTime, actor
        );
        return breakTime.getId();
    }

    @Override
    public void updateBreakTime(ShopBreakTimeUpdateCommand command) {
        Long adminId = command.adminId();
        Long breakTimeId = command.breakTimeId();
        String dayType = command.dayType();
        LocalTime startTime = command.startTime();
        LocalTime endTime = command.endTime();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.updateBreakTime(breakTimeId, DayType.from(dayType), startTime, endTime, actor);
    }

    @Override
    public void deleteBreakTime(ShopBreakTimeDeleteCommand command) {
        Long adminId = command.adminId();
        Long breakTimeId = command.breakTimeId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteBreakTime(breakTimeId, actor);
    }

    @Override
    public Long createClosedDay(ShopClosedDayCreateCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        String closedDayType = command.closedDayType();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopClosedDay closedDay = shopBusinessHourService.createClosedDay(id, ClosedDayType.from(closedDayType), actor);
        return closedDay.getId();
    }

    @Override
    public void deleteClosedDay(ShopClosedDayDeleteCommand command) {
        Long adminId = command.adminId();
        Long closedDayId = command.closedDayId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteClosedDay(closedDayId, actor);
    }

    @Override
    public Long createAmenityCategory(ShopAmenityCategoryCreateCommand command) {
        String amenity = command.amenity();
        String displayName = command.displayName();
        Long activeImageFileId = command.activeImageFileId();
        Long inactiveImageFileId = command.inactiveImageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

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

    @Override
    public void updateAmenityCategory(ShopAmenityCategoryUpdateCommand command) {
        Long categoryId = command.categoryId();
        String displayName = command.displayName();
        Long activeImageFileId = command.activeImageFileId();
        Long inactiveImageFileId = command.inactiveImageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

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

    @Override
    public Long createFoodTypeCategory(ShopFoodTypeCategoryCreateCommand command) {
        String foodType = command.foodType();
        String displayName = command.displayName();
        Long activeImageFileId = command.activeImageFileId();
        Long inactiveImageFileId = command.inactiveImageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

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

    @Override
    public void updateFoodTypeCategory(ShopFoodTypeCategoryUpdateCommand command) {
        Long categoryId = command.categoryId();
        String displayName = command.displayName();
        Long activeImageFileId = command.activeImageFileId();
        Long inactiveImageFileId = command.inactiveImageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

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
    @Override
    public Long assignAmenity(ShopAmenityAssignCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        return shopConvenienceInfoService.assignAmenity(id, amenityCategoryId, actor);
    }

    /**
     * 가게에 배정된 편의시설을 해제한다. 변경이력({@code AMENITY})은 도메인 서비스가 남긴다.
     */
    @Override
    public void unassignAmenity(ShopAmenityUnassignCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopConvenienceInfoService.unassignAmenity(id, amenityCategoryId, actor);
    }

    @Override
    public Long assignFoodType(ShopFoodTypeAssignCommand command) {
        Long id = command.shopId();
        Long foodTypeCategoryId = command.foodTypeCategoryId();

        shopDetailRepository.findFoodTypeCategoryById(foodTypeCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND));
        ShopFoodType foodType = shopDetailRepository.saveFoodType(ShopFoodType.of(ShopId.of(id), ShopFoodTypeCategoryId.of(foodTypeCategoryId)));
        return foodType.getId();
    }

    @Override
    public void unassignFoodType(ShopFoodTypeUnassignCommand command) {
        Long id = command.shopId();
        Long foodTypeCategoryId = command.foodTypeCategoryId();

        shopDetailRepository.deleteFoodTypeByShopIdAndCategoryId(id, foodTypeCategoryId);
    }

    @Override
    public Long createTag(TagCreateCommand command) {
        String tagName = command.tagName();

        Tag tag = tagRepository.save(Tag.of(tagName));
        return tag.getId();
    }

    @Override
    public void deleteTag(TagDeleteCommand command) {
        Long id = command.tagId();

        tagRepository.deleteById(id);
    }

    @Override
    public Long assignOrderMethod(ShopOrderMethodAssignCommand command) {
        Long id = command.shopId();
        String orderMethod = command.orderMethod();

        ShopOrderMethod saved = shopDetailRepository.saveOrderMethod(
            ShopOrderMethod.of(ShopId.of(id), OrderMethod.from(orderMethod))
        );
        return saved.getId();
    }

    @Override
    public void unassignOrderMethod(ShopOrderMethodUnassignCommand command) {
        Long id = command.shopId();
        String orderMethod = command.orderMethod();

        shopDetailRepository.deleteOrderMethodByShopIdAndOrderMethod(id, OrderMethod.from(orderMethod));
    }

    @Override
    public Long createBannerImage(ShopBannerImageCreateCommand command) {
        Long id = command.shopId();
        Long imageFileId = command.imageFileId();
        Integer sort = command.sort();

        ShopBannerImage bannerImage = shopDetailRepository.saveBannerImage(
            ShopBannerImage.of(ShopId.of(id), UploadedFileId.of(imageFileId), sort)
        );
        return bannerImage.getId();
    }

    @Override
    public void deleteBannerImage(ShopBannerImageDeleteCommand command) {
        Long bannerImageId = command.bannerImageId();

        shopDetailRepository.deleteBannerImageById(bannerImageId);
    }

    @Override
    public Long createPhotoCategory(ShopPhotoCategoryCreateCommand command) {
        Long id = command.shopId();
        String name = command.name();

        ShopPhotoCategory photoCategory = shopDetailRepository.savePhotoCategory(ShopPhotoCategory.of(ShopId.of(id), name));
        return photoCategory.getId();
    }

    @Override
    public void updatePhotoCategory(ShopPhotoCategoryUpdateCommand command) {
        Long categoryId = command.categoryId();
        String name = command.name();

        ShopPhotoCategory photoCategory = shopDetailRepository.findPhotoCategoryById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_PHOTO_CATEGORY_NOT_FOUND));
        photoCategory.update(name);
        shopDetailRepository.savePhotoCategory(photoCategory);
    }

    @Override
    public void deletePhotoCategory(ShopPhotoCategoryDeleteCommand command) {
        Long categoryId = command.categoryId();

        shopDetailRepository.deletePhotoCategoryById(categoryId);
    }

    @Override
    public Long createPhotoCategoryImage(ShopPhotoCategoryImageCreateCommand command) {
        Long categoryId = command.categoryId();
        Long imageFileId = command.imageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

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

    @Override
    public void updatePhotoCategoryImage(ShopPhotoCategoryImageUpdateCommand command) {
        Long imageId = command.imageId();
        Long imageFileId = command.imageFileId();
        Integer sort = command.sort();
        Boolean visible = command.visible();

        ShopPhotoCategoryImage image = shopDetailRepository.findPhotoCategoryImageById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_PHOTO_CATEGORY_IMAGE_NOT_FOUND));
        image.update(UploadedFileId.of(imageFileId), sort, visible);
        shopDetailRepository.savePhotoCategoryImage(image);
    }

    @Override
    public void deletePhotoCategoryImage(ShopPhotoCategoryImageDeleteCommand command) {
        Long imageId = command.imageId();

        shopDetailRepository.deletePhotoCategoryImageById(imageId);
    }

    @Override
    public Long createShopChoice(ShopChoiceCreateCommand command) {
        Long shopId = command.shopId();
        String title = command.title();
        String content = command.content();

        ShopChoice shopChoice = shopChoiceRepository.save(ShopChoice.of(ShopId.of(shopId), title, content));
        return shopChoice.getId();
    }

    @Override
    public void updateShopChoice(ShopChoiceUpdateCommand command) {
        Long id = command.choiceId();
        String title = command.title();
        String content = command.content();

        ShopChoice shopChoice = shopChoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_CHOICE_NOT_FOUND));
        shopChoice.update(title, content);
        shopChoiceRepository.save(shopChoice);
    }

    @Override
    public void deleteShopChoice(ShopChoiceDeleteCommand command) {
        Long id = command.choiceId();

        shopChoiceRepository.deleteById(id);
    }
}
