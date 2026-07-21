package com.tastyhouse.adminapi.shop;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.ClosedDayType;
import com.tastyhouse.core.domain.shop.domain.model.DayType;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenity;
import com.tastyhouse.core.domain.shop.domain.model.ShopAmenityCategory;
import com.tastyhouse.core.domain.shop.domain.model.ShopBannerImage;
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
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.shop.application.ShopCommandService;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.application.dto.ShopSearchCondition;
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
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityAssignmentResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeAssignmentResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopListItemResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.file.FileService;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.response.ShopAmenityCategoryResponse;
import com.tastyhouse.adminapi.shop.response.ShopAmenityResponse;
import com.tastyhouse.adminapi.shop.response.ShopBannerImageItemResponse;
import com.tastyhouse.adminapi.shop.response.ShopBreakTimeResponse;
import com.tastyhouse.adminapi.shop.response.ShopBusinessHourResponse;
import com.tastyhouse.adminapi.shop.response.ShopChoiceDetailResponse;
import com.tastyhouse.adminapi.shop.response.ShopChoiceListItemResponse;
import com.tastyhouse.adminapi.shop.response.ShopClosedDayResponse;
import com.tastyhouse.adminapi.shop.response.ShopDetailResponse;
import com.tastyhouse.adminapi.shop.response.ShopFoodTypeCategoryResponse;
import com.tastyhouse.adminapi.shop.response.ShopFoodTypeResponse;
import com.tastyhouse.adminapi.shop.response.ShopListItemResponse;
import com.tastyhouse.adminapi.shop.response.ShopOrderMethodItemResponse;
import com.tastyhouse.adminapi.shop.response.ShopPhotoCategoryImageItemResponse;
import com.tastyhouse.adminapi.shop.response.ShopPhotoCategoryResponse;
import com.tastyhouse.adminapi.shop.response.StationResponse;
import com.tastyhouse.adminapi.shop.response.TagResponse;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopCommandService shopCommandService;
    private final ShopQueryService shopQueryService;
    private final FileService fileService;

    public List<StationResponse> getStations() {
        return shopQueryService.findAllStations().stream()
            .map(station -> StationResponse.from(station.getId(), station.getStationName()))
            .toList();
    }

    public PaginationResponse<ShopListItemResponse> getShops(
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    ) {
        ShopSearchCondition condition = ShopSearchCondition.of(name, stationId, permanentlyClosed);
        PageResult<ShopListItemResponse> pageResult = shopQueryService.findShops(condition, page, size)
            .map(this::toShopListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private ShopListItemResponse toShopListItemResponse(ShopListItemResult dto) {
        return ShopListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.roadAddress(),
            dto.rating(),
            dto.permanentlyClosed()
        );
    }

    public Long createShop(
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        ShopCreateCommand command = ShopCreateCommand.of(
            stationId, name, latitude, longitude, roadAddress, lotAddress, phoneNumber, thumbnailImageFileId
        );
        Shop shop = shopCommandService.createShop(command);
        return shop.getId();
    }

    public ShopDetailResponse getShop(Long id) {
        ShopId shopId = ShopId.of(id);
        Shop shop = shopQueryService.findShopById(shopId);
        return toShopDetailResponse(shop);
    }

    private ShopDetailResponse toShopDetailResponse(Shop shop) {
        return ShopDetailResponse.from(
            shop.getId(),
            shop.getStationId(),
            shop.getName(),
            shop.getLatitude(),
            shop.getLongitude(),
            shop.getRating(),
            shop.getRoadAddress(),
            shop.getLotAddress(),
            shop.getPhoneNumber(),
            shop.getThumbnailImageFileId(),
            shop.isPermanentlyClosed(),
            shop.getCreatedAt(),
            shop.getUpdatedAt()
        );
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
        ShopUpdateCommand command = ShopUpdateCommand.of(
            stationId, name, latitude, longitude, roadAddress, lotAddress, phoneNumber, thumbnailImageFileId
        );
        shopCommandService.updateShop(shopId, command);
    }

    public void closeShop(Long id) {
        ShopId shopId = ShopId.of(id);
        shopCommandService.closeShop(shopId);
    }

    public List<ShopBusinessHourResponse> getBusinessHours(Long id) {
        return shopQueryService.findShopBusinessHours(id).stream()
            .map(this::toShopBusinessHourResponse)
            .toList();
    }

    private ShopBusinessHourResponse toShopBusinessHourResponse(ShopBusinessHour businessHour) {
        return ShopBusinessHourResponse.from(
            businessHour.getId(),
            businessHour.getDayType().name(),
            businessHour.getDayType().getDescription(),
            businessHour.getOpenTime(),
            businessHour.getCloseTime(),
            businessHour.getIsClosed()
        );
    }

    public Long createBusinessHour(Long id, String dayType, LocalTime openTime, LocalTime closeTime, Boolean isClosed) {
        ShopBusinessHourSaveCommand command = ShopBusinessHourSaveCommand.of(DayType.from(dayType), openTime, closeTime, isClosed);
        ShopBusinessHour businessHour = shopCommandService.createBusinessHour(id, command);
        return businessHour.getId();
    }

    public void updateBusinessHour(Long businessHourId, String dayType, LocalTime openTime, LocalTime closeTime, Boolean isClosed) {
        ShopBusinessHourSaveCommand command = ShopBusinessHourSaveCommand.of(DayType.from(dayType), openTime, closeTime, isClosed);
        shopCommandService.updateBusinessHour(businessHourId, command);
    }

    public void deleteBusinessHour(Long businessHourId) {
        shopCommandService.deleteBusinessHour(businessHourId);
    }

    public List<ShopBreakTimeResponse> getBreakTimes(Long id) {
        return shopQueryService.findShopBreakTimes(id).stream()
            .map(this::toShopBreakTimeResponse)
            .toList();
    }

    private ShopBreakTimeResponse toShopBreakTimeResponse(ShopBreakTime breakTime) {
        return ShopBreakTimeResponse.from(
            breakTime.getId(),
            breakTime.getDayType().name(),
            breakTime.getDayType().getDescription(),
            breakTime.getStartTime(),
            breakTime.getEndTime()
        );
    }

    public Long createBreakTime(Long id, String dayType, LocalTime startTime, LocalTime endTime) {
        ShopBreakTimeSaveCommand command = ShopBreakTimeSaveCommand.of(DayType.from(dayType), startTime, endTime);
        ShopBreakTime breakTime = shopCommandService.createBreakTime(id, command);
        return breakTime.getId();
    }

    public void updateBreakTime(Long breakTimeId, String dayType, LocalTime startTime, LocalTime endTime) {
        ShopBreakTimeSaveCommand command = ShopBreakTimeSaveCommand.of(DayType.from(dayType), startTime, endTime);
        shopCommandService.updateBreakTime(breakTimeId, command);
    }

    public void deleteBreakTime(Long breakTimeId) {
        shopCommandService.deleteBreakTime(breakTimeId);
    }

    public List<ShopClosedDayResponse> getClosedDays(Long id) {
        return shopQueryService.findShopClosedDays(id).stream()
            .map(this::toShopClosedDayResponse)
            .toList();
    }

    private ShopClosedDayResponse toShopClosedDayResponse(ShopClosedDay closedDay) {
        return ShopClosedDayResponse.from(
            closedDay.getId(),
            closedDay.getClosedDayType().name(),
            closedDay.getClosedDayType().getDescription()
        );
    }

    public Long createClosedDay(Long id, String closedDayType) {
        ShopClosedDaySaveCommand command = ShopClosedDaySaveCommand.of(ClosedDayType.from(closedDayType));
        ShopClosedDay closedDay = shopCommandService.createClosedDay(id, command);
        return closedDay.getId();
    }

    public void deleteClosedDay(Long closedDayId) {
        shopCommandService.deleteClosedDay(closedDayId);
    }

    public List<ShopAmenityCategoryResponse> getAmenityCategories() {
        return shopQueryService.findAmenityCategories().stream()
            .map(this::toShopAmenityCategoryResponse)
            .toList();
    }

    private ShopAmenityCategoryResponse toShopAmenityCategoryResponse(ShopAmenityCategory category) {
        return ShopAmenityCategoryResponse.from(
            category.getId(),
            category.getAmenity().name(),
            category.getDisplayName(),
            category.getActiveImageFileId(),
            category.getInactiveImageFileId(),
            category.getSort(),
            category.isVisible()
        );
    }

    public Long createAmenityCategory(String amenity, String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, Boolean visible) {
        ShopAmenityCategorySaveCommand command = ShopAmenityCategorySaveCommand.of(
            Amenity.from(amenity), displayName, activeImageFileId, inactiveImageFileId, sort, visible
        );
        ShopAmenityCategory category = shopCommandService.createAmenityCategory(command);
        return category.getId();
    }

    public void updateAmenityCategory(Long categoryId, String amenity, String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, Boolean visible) {
        ShopAmenityCategorySaveCommand command = ShopAmenityCategorySaveCommand.of(
            Amenity.from(amenity), displayName, activeImageFileId, inactiveImageFileId, sort, visible
        );
        shopCommandService.updateAmenityCategory(categoryId, command);
    }

    public List<ShopFoodTypeCategoryResponse> getFoodTypeCategories() {
        return shopQueryService.findFoodTypeCategories().stream()
            .map(this::toShopFoodTypeCategoryResponse)
            .toList();
    }

    private ShopFoodTypeCategoryResponse toShopFoodTypeCategoryResponse(ShopFoodTypeCategory category) {
        return ShopFoodTypeCategoryResponse.from(
            category.getId(),
            category.getFoodType().name(),
            category.getDisplayName(),
            category.getActiveImageFileId(),
            category.getInactiveImageFileId(),
            category.getSort(),
            category.isVisible()
        );
    }

    public Long createFoodTypeCategory(String foodType, String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, Boolean visible) {
        ShopFoodTypeCategorySaveCommand command = ShopFoodTypeCategorySaveCommand.of(
            FoodType.from(foodType), displayName, activeImageFileId, inactiveImageFileId, sort, visible
        );
        ShopFoodTypeCategory category = shopCommandService.createFoodTypeCategory(command);
        return category.getId();
    }

    public void updateFoodTypeCategory(Long categoryId, String foodType, String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, Boolean visible) {
        ShopFoodTypeCategorySaveCommand command = ShopFoodTypeCategorySaveCommand.of(
            FoodType.from(foodType), displayName, activeImageFileId, inactiveImageFileId, sort, visible
        );
        shopCommandService.updateFoodTypeCategory(categoryId, command);
    }

    public List<ShopAmenityResponse> getShopAmenities(Long id) {
        return shopQueryService.findShopAmenityAssignments(id).stream()
            .map(this::toShopAmenityResponse)
            .toList();
    }

    private ShopAmenityResponse toShopAmenityResponse(ShopAmenityAssignmentResult dto) {
        return ShopAmenityResponse.from(
            dto.id(),
            dto.amenityCategoryId(),
            dto.amenity().name(),
            dto.displayName(),
            fileService.getUrlByPath(dto.activeFilePath())
        );
    }

    public Long assignAmenity(Long id, Long amenityCategoryId) {
        ShopAmenity amenity = shopCommandService.assignAmenity(id, amenityCategoryId);
        return amenity.getId();
    }

    public void unassignAmenity(Long id, Long amenityCategoryId) {
        shopCommandService.unassignAmenity(id, amenityCategoryId);
    }

    public List<ShopFoodTypeResponse> getShopFoodTypes(Long id) {
        return shopQueryService.findShopFoodTypeAssignments(id).stream()
            .map(this::toShopFoodTypeResponse)
            .toList();
    }

    private ShopFoodTypeResponse toShopFoodTypeResponse(ShopFoodTypeAssignmentResult dto) {
        return ShopFoodTypeResponse.from(
            dto.id(),
            dto.foodTypeCategoryId(),
            dto.foodType().name(),
            dto.displayName(),
            fileService.getUrlByPath(dto.activeFilePath())
        );
    }

    public Long assignFoodType(Long id, Long foodTypeCategoryId) {
        ShopFoodType foodType = shopCommandService.assignFoodType(id, foodTypeCategoryId);
        return foodType.getId();
    }

    public void unassignFoodType(Long id, Long foodTypeCategoryId) {
        shopCommandService.unassignFoodType(id, foodTypeCategoryId);
    }

    public List<TagResponse> getTags() {
        return shopQueryService.findAllTags().stream()
            .map(tag -> TagResponse.from(tag.getId(), tag.getTagName()))
            .toList();
    }

    public Long createTag(String tagName) {
        Tag tag = shopCommandService.createTag(tagName);
        return tag.getId();
    }

    public void deleteTag(Long id) {
        shopCommandService.deleteTag(id);
    }

    public List<ShopOrderMethodItemResponse> getOrderMethods(Long id) {
        return shopQueryService.findShopOrderMethods(id).stream()
            .map(this::toShopOrderMethodItemResponse)
            .toList();
    }

    private ShopOrderMethodItemResponse toShopOrderMethodItemResponse(ShopOrderMethod orderMethod) {
        return ShopOrderMethodItemResponse.from(
            orderMethod.getId(),
            orderMethod.getOrderMethod().name(),
            orderMethod.getOrderMethod().getDisplayName()
        );
    }

    public Long assignOrderMethod(Long id, String orderMethod) {
        ShopOrderMethodAssignCommand command = ShopOrderMethodAssignCommand.of(OrderMethod.from(orderMethod));
        ShopOrderMethod saved = shopCommandService.assignOrderMethod(id, command);
        return saved.getId();
    }

    public void unassignOrderMethod(Long id, String orderMethod) {
        ShopOrderMethodAssignCommand command = ShopOrderMethodAssignCommand.of(OrderMethod.from(orderMethod));
        shopCommandService.unassignOrderMethod(id, command);
    }

    public List<ShopBannerImageItemResponse> getBannerImages(Long id) {
        return shopQueryService.findShopBannerImageEntities(id).stream()
            .map(image -> ShopBannerImageItemResponse.from(image.getId(), image.getImageFileId(), image.getSort()))
            .toList();
    }

    public Long createBannerImage(Long id, Long imageFileId, Integer sort) {
        ShopBannerImageSaveCommand command = ShopBannerImageSaveCommand.of(imageFileId, sort);
        ShopBannerImage bannerImage = shopCommandService.createBannerImage(id, command);
        return bannerImage.getId();
    }

    public void deleteBannerImage(Long bannerImageId) {
        shopCommandService.deleteBannerImage(bannerImageId);
    }

    public List<ShopPhotoCategoryResponse> getPhotoCategories(Long id) {
        return shopQueryService.findShopPhotoCategoriesByShopId(id).stream()
            .map(category -> ShopPhotoCategoryResponse.from(category.getId(), category.getName()))
            .toList();
    }

    public Long createPhotoCategory(Long id, String name) {
        ShopPhotoCategory photoCategory = shopCommandService.createPhotoCategory(id, name);
        return photoCategory.getId();
    }

    public void updatePhotoCategory(Long categoryId, String name) {
        shopCommandService.updatePhotoCategory(categoryId, name);
    }

    public void deletePhotoCategory(Long categoryId) {
        shopCommandService.deletePhotoCategory(categoryId);
    }

    public List<ShopPhotoCategoryImageItemResponse> getPhotoCategoryImages(Long categoryId) {
        return shopQueryService.findShopPhotoCategoryImages(categoryId).stream()
            .map(this::toShopPhotoCategoryImageItemResponse)
            .toList();
    }

    private ShopPhotoCategoryImageItemResponse toShopPhotoCategoryImageItemResponse(ShopPhotoCategoryImage image) {
        return ShopPhotoCategoryImageItemResponse.from(
            image.getId(),
            image.getShopPhotoCategoryId(),
            image.getImageFileId(),
            image.getSort(),
            image.isVisible()
        );
    }

    public Long createPhotoCategoryImage(Long categoryId, Long imageFileId, Integer sort, Boolean visible) {
        ShopPhotoCategoryImageSaveCommand command = ShopPhotoCategoryImageSaveCommand.of(imageFileId, sort, visible);
        ShopPhotoCategoryImage image = shopCommandService.createPhotoCategoryImage(categoryId, command);
        return image.getId();
    }

    public void updatePhotoCategoryImage(Long imageId, Long imageFileId, Integer sort, Boolean visible) {
        ShopPhotoCategoryImageSaveCommand command = ShopPhotoCategoryImageSaveCommand.of(imageFileId, sort, visible);
        shopCommandService.updatePhotoCategoryImage(imageId, command);
    }

    public void deletePhotoCategoryImage(Long imageId) {
        shopCommandService.deletePhotoCategoryImage(imageId);
    }

    public PaginationResponse<ShopChoiceListItemResponse> getShopChoices(int page, int size) {
        PageResult<ShopChoiceListItemResponse> pageResult = shopQueryService.findEditorChoices(page, size)
            .map(dto -> ShopChoiceListItemResponse.from(dto.id(), dto.shopId(), dto.name(), dto.title()));
        return PaginationResponse.from(pageResult);
    }

    public Long createShopChoice(Long shopId, String title, String content) {
        ShopChoiceSaveCommand command = ShopChoiceSaveCommand.of(title, content);
        ShopChoice shopChoice = shopCommandService.createShopChoice(shopId, command);
        return shopChoice.getId();
    }

    public ShopChoiceDetailResponse getShopChoice(Long id) {
        ShopChoice shopChoice = shopQueryService.findShopChoiceById(id);
        return ShopChoiceDetailResponse.from(shopChoice.getId(), shopChoice.getShopId(), shopChoice.getTitle(), shopChoice.getContent());
    }

    public void updateShopChoice(Long id, String title, String content) {
        ShopChoiceSaveCommand command = ShopChoiceSaveCommand.of(title, content);
        shopCommandService.updateShopChoice(id, command);
    }

    public void deleteShopChoice(Long id) {
        shopCommandService.deleteShopChoice(id);
    }
}
