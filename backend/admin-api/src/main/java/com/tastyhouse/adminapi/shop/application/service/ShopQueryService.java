package com.tastyhouse.adminapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceQueryPort;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeAssignmentResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageManagementResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopManagementDetailResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.application.shop.port.out.ShopSearchCondition;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopAmenityCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopAmenityResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopBannerImageItemResponse;
import com.tastyhouse.apicommon.shop.response.ShopBreakTimeResponse;
import com.tastyhouse.apicommon.shop.response.ShopBusinessHourResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopChoiceDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopChoiceListItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopClosedDayResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopFoodTypeCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopFoodTypeResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopListItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopOrderMethodItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopPhotoCategoryImageItemResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopPhotoCategoryResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.StationResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.TagResponse;
import com.tastyhouse.adminapi.shop.application.port.in.ShopQueryUseCase;

/**
 * admin용 가게 관리 조회 서비스(CQRS query 측).
 *
 * <p>표현 목적 조회는 전부 읽기 포트에서 Result를 받아 Response로 조립한다. 가게 단건 관리 상세도
 * 마찬가지라 write 포트를 주입하지 않는다.
 */
@Service
@Transactional(readOnly = true)
public class ShopQueryService implements ShopQueryUseCase {

    private final ShopQueryPort shopQueryPort;
    private final ShopSearchQueryPort shopSearchQueryPort;
    private final ShopChoiceQueryPort shopChoiceQueryPort;

    public ShopQueryService(
        ShopQueryPort shopQueryPort,
        ShopSearchQueryPort shopSearchQueryPort,
        ShopChoiceQueryPort shopChoiceQueryPort
    ) {
        this.shopQueryPort = shopQueryPort;
        this.shopSearchQueryPort = shopSearchQueryPort;
        this.shopChoiceQueryPort = shopChoiceQueryPort;
    }

    @Override
    public List<StationResponse> getStations() {
        return shopChoiceQueryPort.findAllStations().stream()
            .map(station -> StationResponse.from(station.id(), station.stationName()))
            .toList();
    }

    @Override
    public PaginationResponse<ShopListItemResponse> getShops(
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    ) {
        ShopSearchCondition condition = ShopSearchCondition.of(name, stationId, permanentlyClosed);
        PageResult<ShopListItemResponse> pageResult =
            shopSearchQueryPort.findShops(condition, PageQuery.of(page, size))
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

    @Override
    public ShopDetailResponse getShop(Long id) {
        ShopManagementDetailResult shop = shopQueryPort.findManagementDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        return toShopDetailResponse(shop);
    }

    private ShopDetailResponse toShopDetailResponse(ShopManagementDetailResult shop) {
        String thumbnailImageUrl = shopQueryPort.findShopImageUrls(shop.id())
            .map(ShopImageUrlsResult::thumbnailImageUrl)
            .orElse(null);

        return ShopDetailResponse.from(
            shop.id(),
            shop.stationId(),
            shop.name(),
            shop.latitude(),
            shop.longitude(),
            shop.rating(),
            shop.roadAddress(),
            shop.lotAddress(),
            shop.phoneNumber(),
            thumbnailImageUrl,
            shop.permanentlyClosed(),
            shop.cupDepositEnabled(),
            shop.createdAt(),
            shop.updatedAt()
        );
    }

    @Override
    public List<ShopBusinessHourResponse> getBusinessHours(Long id) {
        return shopQueryPort.findBusinessHours(id).stream()
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

    @Override
    public List<ShopBreakTimeResponse> getBreakTimes(Long id) {
        return shopQueryPort.findBreakTimes(id).stream()
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

    @Override
    public List<ShopClosedDayResponse> getClosedDays(Long id) {
        return shopQueryPort.findClosedDays(id).stream()
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

    @Override
    public List<ShopAmenityCategoryResponse> getAmenityCategories() {
        return shopQueryPort.findAllAmenityCategories().stream()
            .map(this::toShopAmenityCategoryResponse)
            .toList();
    }

    private ShopAmenityCategoryResponse toShopAmenityCategoryResponse(ShopAmenityCategoryResult dto) {
        return ShopAmenityCategoryResponse.from(
            dto.id(),
            dto.amenity().name(),
            dto.displayName(),
            dto.activeIconUrl(),
            dto.inactiveIconUrl(),
            dto.sort(),
            dto.visible()
        );
    }

    @Override
    public List<ShopFoodTypeCategoryResponse> getFoodTypeCategories() {
        return shopQueryPort.findAllFoodTypeCategories().stream()
            .map(this::toShopFoodTypeCategoryResponse)
            .toList();
    }

    private ShopFoodTypeCategoryResponse toShopFoodTypeCategoryResponse(ShopFoodTypeCategoryResult dto) {
        return ShopFoodTypeCategoryResponse.from(
            dto.id(),
            dto.foodType().name(),
            dto.displayName(),
            dto.activeIconUrl(),
            dto.inactiveIconUrl(),
            dto.sort(),
            dto.visible()
        );
    }

    @Override
    public List<ShopAmenityResponse> getShopAmenities(Long id) {
        return shopQueryPort.findAmenityAssignments(id).stream()
            .map(this::toShopAmenityResponse)
            .toList();
    }

    private ShopAmenityResponse toShopAmenityResponse(ShopAmenityAssignmentResult dto) {
        return ShopAmenityResponse.from(
            dto.id(),
            dto.amenityCategoryId(),
            dto.amenity().name(),
            dto.displayName(),
            dto.activeIconUrl()
        );
    }

    @Override
    public List<ShopFoodTypeResponse> getShopFoodTypes(Long id) {
        return shopQueryPort.findFoodTypeAssignments(id).stream()
            .map(this::toShopFoodTypeResponse)
            .toList();
    }

    private ShopFoodTypeResponse toShopFoodTypeResponse(ShopFoodTypeAssignmentResult dto) {
        return ShopFoodTypeResponse.from(
            dto.id(),
            dto.foodTypeCategoryId(),
            dto.foodType().name(),
            dto.displayName(),
            dto.activeIconUrl()
        );
    }

    @Override
    public List<TagResponse> getTags() {
        return shopChoiceQueryPort.findAllTags().stream()
            .map(tag -> TagResponse.from(tag.id(), tag.tagName()))
            .toList();
    }

    @Override
    public List<ShopOrderMethodItemResponse> getOrderMethods(Long id) {
        return shopQueryPort.findOrderMethods(id).stream()
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

    @Override
    public List<ShopBannerImageItemResponse> getBannerImages(Long id) {
        return shopQueryPort.findBannerImages(id).stream()
            .map(image -> ShopBannerImageItemResponse.from(
                image.id(),
                image.imageUrl(),
                image.sort()
            ))
            .toList();
    }

    @Override
    public List<ShopPhotoCategoryResponse> getPhotoCategories(Long id) {
        return shopQueryPort.findPhotoCategories(id).stream()
            .map(this::toShopPhotoCategoryResponse)
            .toList();
    }

    private ShopPhotoCategoryResponse toShopPhotoCategoryResponse(ShopPhotoCategoryResult category) {
        return ShopPhotoCategoryResponse.from(category.id(), category.name());
    }

    @Override
    public List<ShopPhotoCategoryImageItemResponse> getPhotoCategoryImages(Long categoryId) {
        return shopQueryPort.findPhotoCategoryImages(categoryId).stream()
            .map(this::toShopPhotoCategoryImageItemResponse)
            .toList();
    }

    private ShopPhotoCategoryImageItemResponse toShopPhotoCategoryImageItemResponse(ShopPhotoCategoryImageManagementResult dto) {
        return ShopPhotoCategoryImageItemResponse.from(
            dto.id(),
            dto.shopPhotoCategoryId(),
            dto.imageUrl(),
            dto.sort(),
            dto.visible()
        );
    }

    @Override
    public PaginationResponse<ShopChoiceListItemResponse> getShopChoices(int page, int size) {
        PageResult<ShopChoiceListItemResponse> pageResult =
            shopChoiceQueryPort.findEditorChoices(PageQuery.of(page, size))
                .map(dto -> ShopChoiceListItemResponse.from(dto.id(), dto.shopId(), dto.name(), dto.title()));
        return PaginationResponse.from(pageResult);
    }

    @Override
    public ShopChoiceDetailResponse getShopChoice(Long id) {
        return shopChoiceQueryPort.findShopChoiceById(id)
            .map(dto -> ShopChoiceDetailResponse.from(dto.id(), dto.shopId(), dto.title(), dto.content()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_CHOICE_NOT_FOUND));
    }

}
