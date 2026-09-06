package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopAmenityManagementAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityAssignUseCase;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopAmenityManagementUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopAmenityUnassignUseCase;
import com.tastyhouse.application.shop.port.in.ShopBannerImageCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBannerImageCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBannerImageDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeManagementUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBreakTimeUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourManagementUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopBusinessHourUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopCeoAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopCeoAssignUseCase;
import com.tastyhouse.application.shop.port.in.ShopCeoRevokeCommand;
import com.tastyhouse.application.shop.port.in.ShopCeoRevokeUseCase;
import com.tastyhouse.application.shop.port.in.ShopChoiceCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopChoiceDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopChoiceUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopChoiceUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopCloseCommand;
import com.tastyhouse.application.shop.port.in.ShopCloseUseCase;
import com.tastyhouse.application.shop.port.in.ShopClosedDayManagementCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopClosedDayCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopClosedDayManagementDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopClosedDayDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopCupDepositChangeCommand;
import com.tastyhouse.application.shop.port.in.ShopCupDepositChangeUseCase;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeAssignUseCase;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopFoodTypeUnassignUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodAssignUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodUnassignUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryImageUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopPhotoCategoryUpdateUseCase;
import com.tastyhouse.application.shop.port.in.ShopUpdateCommand;
import com.tastyhouse.application.shop.port.in.ShopUpdateUseCase;
import com.tastyhouse.application.shop.port.in.TagCreateCommand;
import com.tastyhouse.application.shop.port.in.TagCreateUseCase;
import com.tastyhouse.application.shop.port.in.TagDeleteCommand;
import com.tastyhouse.application.shop.port.in.TagDeleteUseCase;

import com.tastyhouse.application.shared.marker.AdminApp;
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

@Service
@AdminApp
@Transactional
public class ShopManagementCommandService implements
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

    public ShopManagementCommandService(
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

    @Override
    public void assignCeo(ShopCeoAssignCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        Long ceoId = command.ceoId();

        ShopId shopId = ShopId.of(id);
        CeoId targetCeoId = CeoId.of(ceoId);
        shopCeoAssignmentService.assign(shopId, targetCeoId, adminId);
    }

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

    @Override
    public void changeCupDepositEnabled(ShopCupDepositChangeCommand command) {
        Long id = command.shopId();
        boolean enabled = command.enabled();

        ShopId shopId = ShopId.of(id);
        shopLifecycleService.changeCupDepositEnabled(shopId, enabled);
    }

    @Override
    public Long createBusinessHour(ShopBusinessHourManagementCreateCommand command) {
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
    public void updateBusinessHour(ShopBusinessHourManagementUpdateCommand command) {
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
    public void deleteBusinessHour(ShopBusinessHourManagementDeleteCommand command) {
        Long adminId = command.adminId();
        Long businessHourId = command.businessHourId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteBusinessHour(businessHourId, actor);
    }

    @Override
    public Long createBreakTime(ShopBreakTimeManagementCreateCommand command) {
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
    public void updateBreakTime(ShopBreakTimeManagementUpdateCommand command) {
        Long adminId = command.adminId();
        Long breakTimeId = command.breakTimeId();
        String dayType = command.dayType();
        LocalTime startTime = command.startTime();
        LocalTime endTime = command.endTime();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.updateBreakTime(breakTimeId, DayType.from(dayType), startTime, endTime, actor);
    }

    @Override
    public void deleteBreakTime(ShopBreakTimeManagementDeleteCommand command) {
        Long adminId = command.adminId();
        Long breakTimeId = command.breakTimeId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        shopBusinessHourService.deleteBreakTime(breakTimeId, actor);
    }

    @Override
    public Long createClosedDay(ShopClosedDayManagementCreateCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        String closedDayType = command.closedDayType();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        ShopClosedDay closedDay = shopBusinessHourService.createClosedDay(id, ClosedDayType.from(closedDayType), actor);
        return closedDay.getId();
    }

    @Override
    public void deleteClosedDay(ShopClosedDayManagementDeleteCommand command) {
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

    @Override
    public Long assignAmenity(ShopAmenityManagementAssignCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        Long amenityCategoryId = command.amenityCategoryId();

        ShopChangeActor actor = ShopChangeActor.admin(adminId);
        return shopConvenienceInfoService.assignAmenity(id, amenityCategoryId, actor);
    }

    @Override
    public void unassignAmenity(ShopAmenityManagementUnassignCommand command) {
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
