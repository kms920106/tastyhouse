package com.tastyhouse.webapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.shop.request.ShopMapMarkerSearchRequest;
import com.tastyhouse.webapi.shop.request.ShopReviewSearchRequest;
import com.tastyhouse.webapi.shop.request.ShopSearchRequest;
import com.tastyhouse.webapi.shop.response.ShopAmenityListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopBestListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopEditorChoiceResponse;
import com.tastyhouse.webapi.shop.response.ShopFoodTypeListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopBannerResponse;
import com.tastyhouse.webapi.shop.response.ShopBookmarkResponse;
import com.tastyhouse.webapi.shop.response.ShopDetailResponse;
import com.tastyhouse.webapi.shop.response.ShopInfoResponse;
import com.tastyhouse.webapi.shop.response.ShopLatestListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopMapMarkerResponse;
import com.tastyhouse.webapi.shop.response.ShopOrderMethodResponse;
import com.tastyhouse.webapi.shop.response.ShopPhotoCategoryResponse;
import com.tastyhouse.webapi.shop.response.ShopProductCategoryResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewStatisticsResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewsByRatingPageResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewsByRatingResponse;
import com.tastyhouse.webapi.shop.response.ShopStationListItemResponse;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "가게 관리 API")
public class ShopApiController {

    private final ShopCommandService shopCommandService;
    private final ShopQueryService shopQueryService;

    @Operation(summary = "지도 마커 목록 조회", description = "지도에서 드래그한 위치 기준 주변 가게의 마커 정보(위도, 경도, 상호명)를 조회합니다.")
    @GetMapping("/v1/map/markers")
    public ResponseEntity<ApiResponse<List<ShopMapMarkerResponse>>> getMapMarkers(
        @Valid @ModelAttribute ShopMapMarkerSearchRequest search
    ) {
        List<ShopMapMarkerResponse> markers = shopQueryService.searchMapMarkers(search.latitude(), search.longitude());
        ApiResponse<List<ShopMapMarkerResponse>> response = ApiResponse.success(markers);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "베스트 가게 목록 조회", description = "평점 기준 베스트 가게를 페이징하여 조회합니다. 이미지, 지하철역명, 평점, 가게명, 태그 정보를 포함합니다.")
    @GetMapping("/v1/best")
    public ResponseEntity<ApiResponse<List<ShopBestListItemResponse>>> getBestShops(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = shopQueryService.searchBestShops(pageRequest.page(), pageRequest.size());
        ApiResponse<List<ShopBestListItemResponse>> response = ApiResponse.success(pageResult.content(), pageRequest.page(), pageRequest.size(), pageResult.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "테하 초이스 조회", description = "특정 테하 초이스의 가게 이미지, 제목, 내용, 관련 상품 목록을 조회합니다.")
    @GetMapping("/v1/editor-choice")
    public ResponseEntity<ApiResponse<List<ShopEditorChoiceResponse>>> getEditorChoices(@Valid @ModelAttribute PageRequest pageRequest) {
        List<ShopEditorChoiceResponse> editorChoiceResponses = shopQueryService.searchEditorChoices(pageRequest.page(), pageRequest.size());
        ApiResponse<List<ShopEditorChoiceResponse>> response = ApiResponse.success(editorChoiceResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "최신 가게 목록 조회", description = "최근 등록된 가게를 페이징하여 조회합니다. 이미지, 지하철역명, 평점, 가게명, 태그, 등록일 정보를 포함합니다. 지하철역, 음식종류, 편의시설 필터를 적용할 수 있습니다.")
    @GetMapping("/v1/latest")
    public ResponseEntity<ApiResponse<List<ShopLatestListItemResponse>>> getLatestShops(
        @Valid @ModelAttribute ShopSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        var pageResult = shopQueryService.searchLatestShops(search.stationId(), search.foodTypes(), search.amenities(), pageRequest.page(), pageRequest.size());
        ApiResponse<List<ShopLatestListItemResponse>> response = ApiResponse.success(pageResult.content(), pageRequest.page(), pageRequest.size(), pageResult.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지하철역 목록 조회", description = "지하철역 목록을 가나다라 순으로 조회합니다. ID와 역명을 반환합니다.")
    @GetMapping("/v1/stations")
    public ResponseEntity<ApiResponse<List<ShopStationListItemResponse>>> getStations() {
        List<ShopStationListItemResponse> stations = shopQueryService.searchAllStations();
        ApiResponse<List<ShopStationListItemResponse>> response = ApiResponse.success(stations);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "음식종류 목록 조회", description = "음식종류 전체 목록을 조회합니다. 코드와 표시명을 반환합니다.")
    @GetMapping("/v1/food-types")
    public ResponseEntity<ApiResponse<List<ShopFoodTypeListItemResponse>>> getFoodTypes() {
        List<ShopFoodTypeListItemResponse> foodTypes = shopQueryService.searchAllFoodTypes();
        ApiResponse<List<ShopFoodTypeListItemResponse>> response = ApiResponse.success(foodTypes);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "편의시설 목록 조회", description = "편의시설 전체 목록을 조회합니다. 코드와 표시명을 반환합니다.")
    @GetMapping("/v1/amenities")
    public ResponseEntity<ApiResponse<List<ShopAmenityListItemResponse>>> getAmenities() {
        List<ShopAmenityListItemResponse> amenities = shopQueryService.searchAllAmenities();
        ApiResponse<List<ShopAmenityListItemResponse>> response = ApiResponse.success(amenities);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "가게 상세 조회", description = "가게의 기본 정보를 조회합니다. 상호명, 주소, 위도/경도, 평점, 전화번호, 썸네일 이미지를 포함합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ShopDetailResponse>> getShopDetail(@PathVariable Long id) {
        ShopDetailResponse shopDetail = shopQueryService.getShopDetail(id);
        return ResponseEntity.ok(ApiResponse.success(shopDetail));
    }

    @Operation(summary = "정보 조회", description = "가게의 기본 정보를 조회합니다. 운영시간, 전화번호 등을 포함합니다.")
    @GetMapping("/v1/{id}/info")
    public ResponseEntity<ApiResponse<ShopInfoResponse>> getShopInfo(@PathVariable Long id) {
        ShopInfoResponse shopInfo = shopQueryService.getShopInfo(id);
        ApiResponse<ShopInfoResponse> response = ApiResponse.success(shopInfo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "배너 이미지 조회", description = "가게의 배너 이미지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/banners")
    public ResponseEntity<ApiResponse<List<ShopBannerResponse>>> getShopBanners(@PathVariable Long id) {
        List<ShopBannerResponse> banners = shopQueryService.getShopBanners(id);
        ApiResponse<List<ShopBannerResponse>> response = ApiResponse.success(banners);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 목록 조회", description = "가게의 상품 목록을 조회합니다. 카테고리별로 그룹화되어 반환됩니다.")
    @GetMapping("/v1/{id}/products")
    public ResponseEntity<ApiResponse<List<ShopProductCategoryResponse>>> getShopProducts(@PathVariable Long id) {
        List<ShopProductCategoryResponse> products = shopQueryService.getShopProducts(id);
        ApiResponse<List<ShopProductCategoryResponse>> response = ApiResponse.success(products);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포토 목록 조회", description = "가게의 사진 목록을 조회합니다. 카테고리별로 그룹화되어 반환됩니다.")
    @GetMapping("/v1/{id}/photos")
    public ResponseEntity<ApiResponse<List<ShopPhotoCategoryResponse>>> getShopPhotos(@PathVariable Long id) {
        List<ShopPhotoCategoryResponse> photos = shopQueryService.getShopPhotos(id);
        ApiResponse<List<ShopPhotoCategoryResponse>> response = ApiResponse.success(photos);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 목록 조회", description = "가게의 리뷰 목록을 평점별로 조회합니다. 각 평점(1점~5점)별로 최대 5개씩, 전체 리뷰는 페이지네이션으로 조회합니다.")
    @GetMapping("/v1/{id}/reviews")
    public ResponseEntity<ApiResponse<ShopReviewsByRatingResponse>> getShopReviews(
        @PathVariable Long id,
        @Valid @ModelAttribute ShopReviewSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        ShopReviewsByRatingPageResponse result = shopQueryService.getShopReviewsByRatingWithPagination(id, pageRequest.page(), pageRequest.size(), search.hasImage());
        ApiResponse<ShopReviewsByRatingResponse> response = ApiResponse.success(result.response());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 통계 조회", description = "가게의 리뷰 통계를 조회합니다. 평점, 카테고리별 점수, 재방문의사 등을 포함합니다.")
    @GetMapping("/v1/{id}/reviews/statistics")
    public ResponseEntity<ApiResponse<ShopReviewStatisticsResponse>> getShopReviewStatistics(@PathVariable Long id) {
        ShopReviewStatisticsResponse statistics = shopQueryService.getShopReviewStatistics(id);
        ApiResponse<ShopReviewStatisticsResponse> response = ApiResponse.success(statistics);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "북마크 여부 조회", description = "가게가 현재 사용자에 의해 북마크되었는지 여부를 조회합니다.")
    @GetMapping("/v1/{id}/bookmark")
    public ResponseEntity<ApiResponse<ShopBookmarkResponse>> isBookmarked(
        @PathVariable Long id,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ShopBookmarkResponse bookmarked;
        if (userDetails == null) {
            bookmarked = ShopBookmarkResponse.from(false);
        } else {
            Long memberId = userDetails.getMemberId();
            bookmarked = shopQueryService.isBookmarked(id, memberId);
        }
        return ResponseEntity.ok(ApiResponse.success(bookmarked));
    }

    @Operation(summary = "북마크 토글", description = "가게에 대한 북마크를 추가하거나 제거합니다.")
    @PostMapping("/v1/{id}/bookmark")
    public ResponseEntity<ApiResponse<ShopBookmarkResponse>> toggleBookmark(
        @PathVariable Long id,
        @CurrentUser CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        boolean bookmarked = shopCommandService.toggleBookmark(id, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(ShopBookmarkResponse.from(bookmarked)));
    }

    @Operation(summary = "주문 수단 조회", description = "가게에서 주문 가능한 수단을 조회합니다. 테이블 오더, 예약, 포장 정보를 포함합니다.")
    @GetMapping("/v1/{id}/order-methods")
    public ResponseEntity<ApiResponse<ShopOrderMethodResponse>> getShopOrderMethods(@PathVariable Long id) {
        ShopOrderMethodResponse orderMethods = shopQueryService.getShopOrderMethods(id);
        ApiResponse<ShopOrderMethodResponse> response = ApiResponse.success(orderMethods);
        return ResponseEntity.ok(response);
    }
}
