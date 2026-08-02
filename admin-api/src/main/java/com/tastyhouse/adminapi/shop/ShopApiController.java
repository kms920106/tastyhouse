package com.tastyhouse.adminapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.request.ShopAmenityAssignRequest;
import com.tastyhouse.adminapi.shop.request.ShopAmenityCategoryCreateRequest;
import com.tastyhouse.adminapi.shop.request.ShopAmenityCategoryUpdateRequest;
import com.tastyhouse.adminapi.shop.request.ShopBannerImageSaveRequest;
import com.tastyhouse.adminapi.shop.request.ShopBreakTimeSaveRequest;
import com.tastyhouse.adminapi.shop.request.ShopBusinessHourSaveRequest;
import com.tastyhouse.adminapi.shop.request.ShopChoiceCreateRequest;
import com.tastyhouse.adminapi.shop.request.ShopChoiceSaveRequest;
import com.tastyhouse.adminapi.shop.request.ShopClosedDaySaveRequest;
import com.tastyhouse.adminapi.shop.request.ShopCreateRequest;
import com.tastyhouse.adminapi.shop.request.ShopFoodTypeAssignRequest;
import com.tastyhouse.adminapi.shop.request.ShopFoodTypeCategoryCreateRequest;
import com.tastyhouse.adminapi.shop.request.ShopFoodTypeCategoryUpdateRequest;
import com.tastyhouse.adminapi.shop.request.ShopOrderMethodAssignRequest;
import com.tastyhouse.adminapi.shop.request.ShopPhotoCategoryImageSaveRequest;
import com.tastyhouse.adminapi.shop.request.ShopPhotoCategorySaveRequest;
import com.tastyhouse.adminapi.shop.request.ShopSearchRequest;
import com.tastyhouse.adminapi.shop.request.ShopUpdateRequest;
import com.tastyhouse.adminapi.shop.request.TagCreateRequest;
import com.tastyhouse.adminapi.shop.response.ShopAmenityCategoryResponse;
import com.tastyhouse.adminapi.shop.response.ShopAmenityResponse;
import com.tastyhouse.adminapi.shop.response.ShopBannerImageItemResponse;
import com.tastyhouse.apicommon.shop.response.ShopBreakTimeResponse;
import com.tastyhouse.apicommon.shop.response.ShopBusinessHourResponse;
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

@Tag(name = "Shop Admin", description = "가게 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopApiController {

    private final ShopCommandService shopCommandService;
    private final ShopQueryService shopQueryService;

    @Operation(summary = "지하철역 목록 조회", description = "가게 등록·수정 시 선택 가능한 지하철역 목록을 조회합니다.")
    @GetMapping("/v1/stations")
    public ResponseEntity<ApiResponse<List<StationResponse>>> getStations() {
        List<StationResponse> response = shopQueryService.getStations();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 목록 조회", description = "가게 목록을 조건 페이징 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ShopListItemResponse>>> getShops(
        @Valid @ModelAttribute ShopSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ShopListItemResponse> pageResponse = shopQueryService.getShops(
            search.name(), search.stationId(), search.permanentlyClosed(),
            pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "가게 등록", description = "새로운 가게를 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createShop(@Valid @RequestBody ShopCreateRequest request) {
        Long id = shopCommandService.createShop(
            request.ceoId(), request.stationId(), request.name(), request.latitude(), request.longitude(),
            request.roadAddress(), request.lotAddress(), request.phoneNumber(), request.thumbnailImageFileId()
        );
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "가게 상세 조회", description = "가게 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ShopDetailResponse>> getShop(@PathVariable Long id) {
        ShopDetailResponse response = shopQueryService.getShop(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 수정", description = "기존 가게를 수정합니다. 폐업 처리된 가게는 수정할 수 없습니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateShop(
        @PathVariable Long id,
        @Valid @RequestBody ShopUpdateRequest request
    ) {
        shopCommandService.updateShop(
            id, request.stationId(), request.name(), request.latitude(), request.longitude(),
            request.roadAddress(), request.lotAddress(), request.phoneNumber(), request.thumbnailImageFileId()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 폐업 처리", description = "가게를 폐업 상태로 변경합니다.")
    @PatchMapping("/v1/{id}/close")
    public ResponseEntity<ApiResponse<Void>> closeShop(@PathVariable Long id) {
        shopCommandService.closeShop(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "운영시간 목록 조회", description = "가게의 운영시간 목록을 조회합니다.")
    @GetMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<List<ShopBusinessHourResponse>>> getBusinessHours(@PathVariable Long id) {
        List<ShopBusinessHourResponse> response = shopQueryService.getBusinessHours(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "운영시간 등록", description = "가게에 운영시간을 등록합니다.")
    @PostMapping("/v1/{id}/business-hours")
    public ResponseEntity<ApiResponse<Long>> createBusinessHour(
        @PathVariable Long id,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        Long businessHourId = shopCommandService.createBusinessHour(id, request.dayType(), request.openTime(), request.closeTime(), request.isClosed(), request.is24Hours());
        return ResponseEntity.ok(ApiResponse.success(businessHourId));
    }

    @Operation(summary = "운영시간 수정", description = "등록된 운영시간을 수정합니다.")
    @PutMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> updateBusinessHour(
        @PathVariable Long businessHourId,
        @Valid @RequestBody ShopBusinessHourSaveRequest request
    ) {
        shopCommandService.updateBusinessHour(businessHourId, request.dayType(), request.openTime(), request.closeTime(), request.isClosed(), request.is24Hours());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "운영시간 삭제", description = "등록된 운영시간을 삭제합니다.")
    @DeleteMapping("/v1/business-hours/{businessHourId}")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessHour(@PathVariable Long businessHourId) {
        shopCommandService.deleteBusinessHour(businessHourId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "브레이크타임 목록 조회", description = "가게의 브레이크타임 목록을 조회합니다.")
    @GetMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<List<ShopBreakTimeResponse>>> getBreakTimes(@PathVariable Long id) {
        List<ShopBreakTimeResponse> response = shopQueryService.getBreakTimes(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "브레이크타임 등록", description = "가게에 브레이크타임을 등록합니다.")
    @PostMapping("/v1/{id}/break-times")
    public ResponseEntity<ApiResponse<Long>> createBreakTime(
        @PathVariable Long id,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        Long breakTimeId = shopCommandService.createBreakTime(id, request.dayType(), request.startTime(), request.endTime());
        return ResponseEntity.ok(ApiResponse.success(breakTimeId));
    }

    @Operation(summary = "브레이크타임 수정", description = "등록된 브레이크타임을 수정합니다.")
    @PutMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> updateBreakTime(
        @PathVariable Long breakTimeId,
        @Valid @RequestBody ShopBreakTimeSaveRequest request
    ) {
        shopCommandService.updateBreakTime(breakTimeId, request.dayType(), request.startTime(), request.endTime());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "브레이크타임 삭제", description = "등록된 브레이크타임을 삭제합니다.")
    @DeleteMapping("/v1/break-times/{breakTimeId}")
    public ResponseEntity<ApiResponse<Void>> deleteBreakTime(@PathVariable Long breakTimeId) {
        shopCommandService.deleteBreakTime(breakTimeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "정기 휴무일 목록 조회", description = "가게의 정기 휴무일 목록을 조회합니다.")
    @GetMapping("/v1/{id}/closed-days")
    public ResponseEntity<ApiResponse<List<ShopClosedDayResponse>>> getClosedDays(@PathVariable Long id) {
        List<ShopClosedDayResponse> response = shopQueryService.getClosedDays(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "정기 휴무일 등록", description = "가게에 정기 휴무일을 등록합니다.")
    @PostMapping("/v1/{id}/closed-days")
    public ResponseEntity<ApiResponse<Long>> createClosedDay(
        @PathVariable Long id,
        @Valid @RequestBody ShopClosedDaySaveRequest request
    ) {
        Long closedDayId = shopCommandService.createClosedDay(id, request.closedDayType());
        return ResponseEntity.ok(ApiResponse.success(closedDayId));
    }

    @Operation(summary = "정기 휴무일 삭제", description = "등록된 정기 휴무일을 삭제합니다.")
    @DeleteMapping("/v1/closed-days/{closedDayId}")
    public ResponseEntity<ApiResponse<Void>> deleteClosedDay(@PathVariable Long closedDayId) {
        shopCommandService.deleteClosedDay(closedDayId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "편의시설 카테고리 목록 조회", description = "편의시설 마스터 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/amenity-categories")
    public ResponseEntity<ApiResponse<List<ShopAmenityCategoryResponse>>> getAmenityCategories() {
        List<ShopAmenityCategoryResponse> response = shopQueryService.getAmenityCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "편의시설 카테고리 등록", description = "편의시설 마스터 카테고리를 등록합니다.")
    @PostMapping("/v1/amenity-categories")
    public ResponseEntity<ApiResponse<Long>> createAmenityCategory(@Valid @RequestBody ShopAmenityCategoryCreateRequest request) {
        Long id = shopCommandService.createAmenityCategory(
            request.amenity(), request.displayName(), request.activeImageFileId(), request.inactiveImageFileId(), request.sort(), request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "편의시설 카테고리 수정", description = "편의시설 마스터 카테고리를 수정합니다.")
    @PutMapping("/v1/amenity-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updateAmenityCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopAmenityCategoryUpdateRequest request
    ) {
        shopCommandService.updateAmenityCategory(
            categoryId, request.displayName(), request.activeImageFileId(), request.inactiveImageFileId(), request.sort(), request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "음식종류 카테고리 목록 조회", description = "음식종류 마스터 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/food-type-categories")
    public ResponseEntity<ApiResponse<List<ShopFoodTypeCategoryResponse>>> getFoodTypeCategories() {
        List<ShopFoodTypeCategoryResponse> response = shopQueryService.getFoodTypeCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "음식종류 카테고리 등록", description = "음식종류 마스터 카테고리를 등록합니다.")
    @PostMapping("/v1/food-type-categories")
    public ResponseEntity<ApiResponse<Long>> createFoodTypeCategory(@Valid @RequestBody ShopFoodTypeCategoryCreateRequest request) {
        Long id = shopCommandService.createFoodTypeCategory(
            request.foodType(), request.displayName(), request.activeImageFileId(), request.inactiveImageFileId(), request.sort(), request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "음식종류 카테고리 수정", description = "음식종류 마스터 카테고리를 수정합니다.")
    @PutMapping("/v1/food-type-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updateFoodTypeCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopFoodTypeCategoryUpdateRequest request
    ) {
        shopCommandService.updateFoodTypeCategory(
            categoryId, request.displayName(), request.activeImageFileId(), request.inactiveImageFileId(), request.sort(), request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 편의시설 목록 조회", description = "가게에 지정된 편의시설 목록을 조회합니다.")
    @GetMapping("/v1/{id}/amenities")
    public ResponseEntity<ApiResponse<List<ShopAmenityResponse>>> getShopAmenities(@PathVariable Long id) {
        List<ShopAmenityResponse> response = shopQueryService.getShopAmenities(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 편의시설 지정", description = "가게에 편의시설을 지정합니다.")
    @PostMapping("/v1/{id}/amenities")
    public ResponseEntity<ApiResponse<Long>> assignAmenity(
        @PathVariable Long id,
        @Valid @RequestBody ShopAmenityAssignRequest request
    ) {
        Long amenityId = shopCommandService.assignAmenity(id, request.amenityCategoryId());
        return ResponseEntity.ok(ApiResponse.success(amenityId));
    }

    @Operation(summary = "가게 편의시설 해제", description = "가게에 지정된 편의시설을 해제합니다.")
    @DeleteMapping("/v1/{id}/amenities/{amenityCategoryId}")
    public ResponseEntity<ApiResponse<Void>> unassignAmenity(
        @PathVariable Long id,
        @PathVariable Long amenityCategoryId
    ) {
        shopCommandService.unassignAmenity(id, amenityCategoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 음식종류 목록 조회", description = "가게에 지정된 음식종류 목록을 조회합니다.")
    @GetMapping("/v1/{id}/food-types")
    public ResponseEntity<ApiResponse<List<ShopFoodTypeResponse>>> getShopFoodTypes(@PathVariable Long id) {
        List<ShopFoodTypeResponse> response = shopQueryService.getShopFoodTypes(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 음식종류 지정", description = "가게에 음식종류를 지정합니다.")
    @PostMapping("/v1/{id}/food-types")
    public ResponseEntity<ApiResponse<Long>> assignFoodType(
        @PathVariable Long id,
        @Valid @RequestBody ShopFoodTypeAssignRequest request
    ) {
        Long foodTypeId = shopCommandService.assignFoodType(id, request.foodTypeCategoryId());
        return ResponseEntity.ok(ApiResponse.success(foodTypeId));
    }

    @Operation(summary = "가게 음식종류 해제", description = "가게에 지정된 음식종류를 해제합니다.")
    @DeleteMapping("/v1/{id}/food-types/{foodTypeCategoryId}")
    public ResponseEntity<ApiResponse<Void>> unassignFoodType(
        @PathVariable Long id,
        @PathVariable Long foodTypeCategoryId
    ) {
        shopCommandService.unassignFoodType(id, foodTypeCategoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "태그 목록 조회", description = "태그 목록을 조회합니다.")
    @GetMapping("/v1/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        List<TagResponse> response = shopQueryService.getTags();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "태그 등록", description = "새로운 태그를 등록합니다.")
    @PostMapping("/v1/tags")
    public ResponseEntity<ApiResponse<Long>> createTag(@Valid @RequestBody TagCreateRequest request) {
        Long id = shopCommandService.createTag(request.tagName());
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "태그 삭제", description = "등록된 태그를 삭제합니다.")
    @DeleteMapping("/v1/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long tagId) {
        shopCommandService.deleteTag(tagId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게 주문수단 목록 조회", description = "가게에 지정된 주문수단 목록을 조회합니다.")
    @GetMapping("/v1/{id}/order-methods")
    public ResponseEntity<ApiResponse<List<ShopOrderMethodItemResponse>>> getOrderMethods(@PathVariable Long id) {
        List<ShopOrderMethodItemResponse> response = shopQueryService.getOrderMethods(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 주문수단 지정", description = "가게에 주문수단을 지정합니다.")
    @PostMapping("/v1/{id}/order-methods")
    public ResponseEntity<ApiResponse<Long>> assignOrderMethod(
        @PathVariable Long id,
        @Valid @RequestBody ShopOrderMethodAssignRequest request
    ) {
        Long orderMethodId = shopCommandService.assignOrderMethod(id, request.orderMethod());
        return ResponseEntity.ok(ApiResponse.success(orderMethodId));
    }

    @Operation(summary = "가게 주문수단 해제", description = "가게에 지정된 주문수단을 해제합니다.")
    @DeleteMapping("/v1/{id}/order-methods/{orderMethod}")
    public ResponseEntity<ApiResponse<Void>> unassignOrderMethod(
        @PathVariable Long id,
        @PathVariable String orderMethod
    ) {
        shopCommandService.unassignOrderMethod(id, orderMethod);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "배너 이미지 목록 조회", description = "가게의 배너 이미지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/banners")
    public ResponseEntity<ApiResponse<List<ShopBannerImageItemResponse>>> getBannerImages(@PathVariable Long id) {
        List<ShopBannerImageItemResponse> response = shopQueryService.getBannerImages(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배너 이미지 등록", description = "가게에 배너 이미지를 등록합니다.")
    @PostMapping("/v1/{id}/banners")
    public ResponseEntity<ApiResponse<Long>> createBannerImage(
        @PathVariable Long id,
        @Valid @RequestBody ShopBannerImageSaveRequest request
    ) {
        Long bannerImageId = shopCommandService.createBannerImage(id, request.imageFileId(), request.sort());
        return ResponseEntity.ok(ApiResponse.success(bannerImageId));
    }

    @Operation(summary = "배너 이미지 삭제", description = "등록된 배너 이미지를 삭제합니다.")
    @DeleteMapping("/v1/banners/{bannerImageId}")
    public ResponseEntity<ApiResponse<Void>> deleteBannerImage(@PathVariable Long bannerImageId) {
        shopCommandService.deleteBannerImage(bannerImageId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 목록 조회", description = "가게의 포토 카테고리 목록을 조회합니다.")
    @GetMapping("/v1/{id}/photo-categories")
    public ResponseEntity<ApiResponse<List<ShopPhotoCategoryResponse>>> getPhotoCategories(@PathVariable Long id) {
        List<ShopPhotoCategoryResponse> response = shopQueryService.getPhotoCategories(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "포토 카테고리 등록", description = "가게에 포토 카테고리를 등록합니다.")
    @PostMapping("/v1/{id}/photo-categories")
    public ResponseEntity<ApiResponse<Long>> createPhotoCategory(
        @PathVariable Long id,
        @Valid @RequestBody ShopPhotoCategorySaveRequest request
    ) {
        Long categoryId = shopCommandService.createPhotoCategory(id, request.name());
        return ResponseEntity.ok(ApiResponse.success(categoryId));
    }

    @Operation(summary = "포토 카테고리 수정", description = "등록된 포토 카테고리를 수정합니다.")
    @PutMapping("/v1/photo-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> updatePhotoCategory(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopPhotoCategorySaveRequest request
    ) {
        shopCommandService.updatePhotoCategory(categoryId, request.name());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 삭제", description = "등록된 포토 카테고리를 삭제합니다.")
    @DeleteMapping("/v1/photo-categories/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deletePhotoCategory(@PathVariable Long categoryId) {
        shopCommandService.deletePhotoCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 이미지 목록 조회", description = "포토 카테고리에 속한 이미지 목록을 조회합니다.")
    @GetMapping("/v1/photo-categories/{categoryId}/images")
    public ResponseEntity<ApiResponse<List<ShopPhotoCategoryImageItemResponse>>> getPhotoCategoryImages(@PathVariable Long categoryId) {
        List<ShopPhotoCategoryImageItemResponse> response = shopQueryService.getPhotoCategoryImages(categoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "포토 카테고리 이미지 등록", description = "포토 카테고리에 이미지를 등록합니다.")
    @PostMapping("/v1/photo-categories/{categoryId}/images")
    public ResponseEntity<ApiResponse<Long>> createPhotoCategoryImage(
        @PathVariable Long categoryId,
        @Valid @RequestBody ShopPhotoCategoryImageSaveRequest request
    ) {
        Long imageId = shopCommandService.createPhotoCategoryImage(categoryId, request.imageFileId(), request.sort(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(imageId));
    }

    @Operation(summary = "포토 카테고리 이미지 수정", description = "등록된 포토 카테고리 이미지를 수정합니다.")
    @PutMapping("/v1/photo-categories/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> updatePhotoCategoryImage(
        @PathVariable Long imageId,
        @Valid @RequestBody ShopPhotoCategoryImageSaveRequest request
    ) {
        shopCommandService.updatePhotoCategoryImage(imageId, request.imageFileId(), request.sort(), request.visible());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "포토 카테고리 이미지 삭제", description = "등록된 포토 카테고리 이미지를 삭제합니다.")
    @DeleteMapping("/v1/photo-categories/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deletePhotoCategoryImage(@PathVariable Long imageId) {
        shopCommandService.deletePhotoCategoryImage(imageId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "테하 초이스 목록 조회", description = "테하 초이스 목록을 페이징하여 조회합니다.")
    @GetMapping("/v1/editor-choices")
    public ResponseEntity<ApiResponse<List<ShopChoiceListItemResponse>>> getShopChoices(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<ShopChoiceListItemResponse> pageResponse = shopQueryService.getShopChoices(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "테하 초이스 등록", description = "새로운 테하 초이스를 등록합니다.")
    @PostMapping("/v1/editor-choices")
    public ResponseEntity<ApiResponse<Long>> createShopChoice(@Valid @RequestBody ShopChoiceCreateRequest request) {
        Long id = shopCommandService.createShopChoice(request.shopId(), request.title(), request.content());
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "테하 초이스 상세 조회", description = "테하 초이스 상세를 조회합니다.")
    @GetMapping("/v1/editor-choices/{choiceId}")
    public ResponseEntity<ApiResponse<ShopChoiceDetailResponse>> getShopChoice(@PathVariable Long choiceId) {
        ShopChoiceDetailResponse response = shopQueryService.getShopChoice(choiceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "테하 초이스 수정", description = "등록된 테하 초이스를 수정합니다.")
    @PutMapping("/v1/editor-choices/{choiceId}")
    public ResponseEntity<ApiResponse<Void>> updateShopChoice(
        @PathVariable Long choiceId,
        @Valid @RequestBody ShopChoiceSaveRequest request
    ) {
        shopCommandService.updateShopChoice(choiceId, request.title(), request.content());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "테하 초이스 삭제", description = "등록된 테하 초이스를 삭제합니다.")
    @DeleteMapping("/v1/editor-choices/{choiceId}")
    public ResponseEntity<ApiResponse<Void>> deleteShopChoice(@PathVariable Long choiceId) {
        shopCommandService.deleteShopChoice(choiceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
