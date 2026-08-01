package com.tastyhouse.adminapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.domain.model.Shop;
import com.tastyhouse.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopAmenityAssignmentResult;
import com.tastyhouse.infrastructure.shop.query.ShopAmenityCategoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopBreakTimeResult;
import com.tastyhouse.infrastructure.shop.query.ShopBusinessHourResult;
import com.tastyhouse.infrastructure.shop.query.ShopChoiceQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopClosedDayResult;
import com.tastyhouse.infrastructure.shop.query.ShopFoodTypeAssignmentResult;
import com.tastyhouse.infrastructure.shop.query.ShopFoodTypeCategoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopListItemResult;
import com.tastyhouse.infrastructure.shop.query.ShopOrderMethodResult;
import com.tastyhouse.infrastructure.shop.query.ShopPhotoCategoryImageManagementResult;
import com.tastyhouse.infrastructure.shop.query.ShopPhotoCategoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopSearchCondition;
import com.tastyhouse.infrastructure.shop.query.ShopSearchQueryDao;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.file.FileService;
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

/**
 * admin용 가게 관리 조회 서비스(CQRS query 측).
 *
 * <p>표현 목적 조회는 전부 infra query DAO에서 Result를 받아 Response로 조립한다. 가게 단건 상세만
 * 도메인 모델이 필요해 write 포트({@code ShopRepository})를 쓴다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopQueryService {

    private final ShopRepository shopRepository;
    private final ShopQueryDao shopQueryDao;
    private final ShopSearchQueryDao shopSearchQueryDao;
    private final ShopChoiceQueryDao shopChoiceQueryDao;
    private final FileService fileService;

    public List<StationResponse> getStations() {
        return shopChoiceQueryDao.findAllStations().stream()
            .map(station -> StationResponse.from(station.id(), station.stationName()))
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
        PageResult<ShopListItemResponse> pageResult =
            shopSearchQueryDao.findShops(condition, PageQuery.of(page, size))
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

    public ShopDetailResponse getShop(Long id) {
        ShopId shopId = ShopId.of(id);
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
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
            toImageUrl(shop.getThumbnailImageFileId()),
            shop.isPermanentlyClosed(),
            shop.getCreatedAt(),
            shop.getUpdatedAt()
        );
    }

    public List<ShopBusinessHourResponse> getBusinessHours(Long id) {
        return shopQueryDao.findBusinessHours(id).stream()
            .map(this::toShopBusinessHourResponse)
            .toList();
    }

    private ShopBusinessHourResponse toShopBusinessHourResponse(ShopBusinessHourResult businessHour) {
        return ShopBusinessHourResponse.from(
            businessHour.id(),
            businessHour.dayType().name(),
            businessHour.dayType().getDescription(),
            businessHour.openTime(),
            businessHour.closeTime(),
            businessHour.closed(),
            businessHour.allDay()
        );
    }

    public List<ShopBreakTimeResponse> getBreakTimes(Long id) {
        return shopQueryDao.findBreakTimes(id).stream()
            .map(this::toShopBreakTimeResponse)
            .toList();
    }

    private ShopBreakTimeResponse toShopBreakTimeResponse(ShopBreakTimeResult breakTime) {
        return ShopBreakTimeResponse.from(
            breakTime.id(),
            breakTime.dayType().name(),
            breakTime.dayType().getDescription(),
            breakTime.startTime(),
            breakTime.endTime()
        );
    }

    public List<ShopClosedDayResponse> getClosedDays(Long id) {
        return shopQueryDao.findClosedDays(id).stream()
            .map(this::toShopClosedDayResponse)
            .toList();
    }

    private ShopClosedDayResponse toShopClosedDayResponse(ShopClosedDayResult closedDay) {
        return ShopClosedDayResponse.from(
            closedDay.id(),
            closedDay.closedDayType().name(),
            closedDay.closedDayType().getDescription()
        );
    }

    public List<ShopAmenityCategoryResponse> getAmenityCategories() {
        return shopQueryDao.findAllAmenityCategories().stream()
            .map(this::toShopAmenityCategoryResponse)
            .toList();
    }

    private ShopAmenityCategoryResponse toShopAmenityCategoryResponse(ShopAmenityCategoryResult dto) {
        return ShopAmenityCategoryResponse.from(
            dto.id(),
            dto.amenity().name(),
            dto.displayName(),
            fileService.getUrlByPath(dto.activeFilePath()),
            fileService.getUrlByPath(dto.inactiveFilePath()),
            dto.sort(),
            dto.visible()
        );
    }

    public List<ShopFoodTypeCategoryResponse> getFoodTypeCategories() {
        return shopQueryDao.findAllFoodTypeCategories().stream()
            .map(this::toShopFoodTypeCategoryResponse)
            .toList();
    }

    private ShopFoodTypeCategoryResponse toShopFoodTypeCategoryResponse(ShopFoodTypeCategoryResult dto) {
        return ShopFoodTypeCategoryResponse.from(
            dto.id(),
            dto.foodType().name(),
            dto.displayName(),
            fileService.getUrlByPath(dto.activeFilePath()),
            fileService.getUrlByPath(dto.inactiveFilePath()),
            dto.sort(),
            dto.visible()
        );
    }

    public List<ShopAmenityResponse> getShopAmenities(Long id) {
        return shopQueryDao.findAmenityAssignments(id).stream()
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

    public List<ShopFoodTypeResponse> getShopFoodTypes(Long id) {
        return shopQueryDao.findFoodTypeAssignments(id).stream()
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

    public List<TagResponse> getTags() {
        return shopChoiceQueryDao.findAllTags().stream()
            .map(tag -> TagResponse.from(tag.id(), tag.tagName()))
            .toList();
    }

    public List<ShopOrderMethodItemResponse> getOrderMethods(Long id) {
        return shopQueryDao.findOrderMethods(id).stream()
            .map(this::toShopOrderMethodItemResponse)
            .toList();
    }

    private ShopOrderMethodItemResponse toShopOrderMethodItemResponse(ShopOrderMethodResult orderMethod) {
        return ShopOrderMethodItemResponse.from(
            orderMethod.id(),
            orderMethod.orderMethod().name(),
            orderMethod.orderMethod().getDisplayName()
        );
    }

    public List<ShopBannerImageItemResponse> getBannerImages(Long id) {
        return shopQueryDao.findBannerImages(id).stream()
            .map(image -> ShopBannerImageItemResponse.from(
                image.id(),
                fileService.getUrlByPath(image.filePath()),
                image.sort()
            ))
            .toList();
    }

    public List<ShopPhotoCategoryResponse> getPhotoCategories(Long id) {
        return shopQueryDao.findPhotoCategories(id).stream()
            .map(this::toShopPhotoCategoryResponse)
            .toList();
    }

    private ShopPhotoCategoryResponse toShopPhotoCategoryResponse(ShopPhotoCategoryResult category) {
        return ShopPhotoCategoryResponse.from(category.id(), category.name());
    }

    public List<ShopPhotoCategoryImageItemResponse> getPhotoCategoryImages(Long categoryId) {
        return shopQueryDao.findPhotoCategoryImages(categoryId).stream()
            .map(this::toShopPhotoCategoryImageItemResponse)
            .toList();
    }

    private ShopPhotoCategoryImageItemResponse toShopPhotoCategoryImageItemResponse(ShopPhotoCategoryImageManagementResult dto) {
        return ShopPhotoCategoryImageItemResponse.from(
            dto.id(),
            dto.shopPhotoCategoryId(),
            fileService.getUrlByPath(dto.filePath()),
            dto.sort(),
            dto.visible()
        );
    }

    public PaginationResponse<ShopChoiceListItemResponse> getShopChoices(int page, int size) {
        PageResult<ShopChoiceListItemResponse> pageResult =
            shopChoiceQueryDao.findEditorChoices(PageQuery.of(page, size))
                .map(dto -> ShopChoiceListItemResponse.from(dto.id(), dto.shopId(), dto.name(), dto.title()));
        return PaginationResponse.from(pageResult);
    }

    public ShopChoiceDetailResponse getShopChoice(Long id) {
        return shopChoiceQueryDao.findShopChoiceById(id)
            .map(dto -> ShopChoiceDetailResponse.from(dto.id(), dto.shopId(), dto.title(), dto.content()))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_CHOICE_NOT_FOUND));
    }

    /**
     * 파일 식별자를 표시용 URL로 변환한다. 식별자가 없으면 null을 돌려준다.
     */
    private String toImageUrl(Long imageFileId) {
        if (imageFileId == null) {
            return null;
        }
        return fileService.getUrlByFileId(imageFileId);
    }
}
