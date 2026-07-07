package com.tastyhouse.webapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.shop.response.AmenityListItemResponse;
import com.tastyhouse.webapi.shop.response.BestShopListItemResponse;
import com.tastyhouse.webapi.shop.response.EditorChoiceResponse;
import com.tastyhouse.webapi.shop.response.FoodTypeListItemResponse;
import com.tastyhouse.webapi.shop.response.LatestShopListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopBannerResponse;
import com.tastyhouse.webapi.shop.response.ShopBookmarkResponse;
import com.tastyhouse.webapi.shop.response.ShopDetailResponse;
import com.tastyhouse.webapi.shop.response.ShopInfoResponse;
import com.tastyhouse.webapi.shop.response.ShopMapMarkerResponse;
import com.tastyhouse.webapi.shop.response.ShopOrderMethodResponse;
import com.tastyhouse.webapi.shop.response.ShopPhotoCategoryResponse;
import com.tastyhouse.webapi.shop.response.ShopProductCategoryResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewStatisticsResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewsByRatingResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewsByRatingWithPagination;
import com.tastyhouse.webapi.shop.response.StationListItemResponse;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "가게 관리 API")
public class ShopApiController {

    private final ShopService shopService;

    @Operation(summary = "지도 마커 목록 조회", description = "지도에서 드래그한 위치 기준 주변 가게의 마커 정보(위도, 경도, 상호명)를 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/map/markers")
    public ResponseEntity<ApiResponse<List<ShopMapMarkerResponse>>> getMapMarkers(
        @RequestParam @Parameter(description = "위도", example = "37.5013") Double latitude,
        @RequestParam @Parameter(description = "경도", example = "127.0396") Double longitude
    ) {
        List<ShopMapMarkerResponse> markers = shopService.searchMapMarkers(latitude, longitude);
        ApiResponse<List<ShopMapMarkerResponse>> response = ApiResponse.success(markers);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "베스트 가게 목록 조회", description = "평점 기준 베스트 가게를 페이징하여 조회합니다. 이미지, 지하철역명, 평점, 가게명, 태그 정보를 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/best")
    public ResponseEntity<ApiResponse<List<BestShopListItemResponse>>> getBestShops(@Valid @ModelAttribute PageRequest pageRequest) {
        var pageResult = shopService.searchBestShops(pageRequest.page(), pageRequest.size());
        ApiResponse<List<BestShopListItemResponse>> response = ApiResponse.success(pageResult.content(), pageRequest.page(), pageRequest.size(), pageResult.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "테하 초이스 조회", description = "특정 테하 초이스의 가게 이미지, 제목, 내용, 관련 상품 목록을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/editor-choice")
    public ResponseEntity<ApiResponse<List<EditorChoiceResponse>>> getEditorChoices(@Valid @ModelAttribute PageRequest pageRequest) {
        List<EditorChoiceResponse> editorChoiceResponses = shopService.searchEditorChoices(pageRequest.page(), pageRequest.size());
        ApiResponse<List<EditorChoiceResponse>> response = ApiResponse.success(editorChoiceResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "최신 가게 목록 조회", description = "최근 등록된 가게를 페이징하여 조회합니다. 이미지, 지하철역명, 평점, 가게명, 태그, 등록일 정보를 포함합니다. 지하철역, 음식종류, 편의시설 필터를 적용할 수 있습니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/latest")
    public ResponseEntity<ApiResponse<List<LatestShopListItemResponse>>> getLatestShops(
        @Valid @ModelAttribute PageRequest pageRequest,
        @RequestParam(required = false) Long stationId,
        @Parameter(schema = @Schema(allowableValues = {"KOREAN", "JAPANESE", "WESTERN", "CHINESE", "WORLD", "SNACK", "BAR", "CAFE"}))
        @RequestParam(required = false) List<String> foodTypes,
        @Parameter(schema = @Schema(allowableValues = {"PARKING", "RESTROOM", "RESERVATION", "BABY_CHAIR", "PET_FRIENDLY", "OUTLET", "TAKEOUT", "DELIVERY"}))
        @RequestParam(required = false) List<String> amenities
    ) {
        var pageResult = shopService.searchLatestShops(stationId, foodTypes, amenities, pageRequest.page(), pageRequest.size());
        ApiResponse<List<LatestShopListItemResponse>> response = ApiResponse.success(pageResult.content(), pageRequest.page(), pageRequest.size(), pageResult.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지하철역 목록 조회", description = "지하철역 목록을 가나다라 순으로 조회합니다. ID와 역명을 반환합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/stations")
    public ResponseEntity<ApiResponse<List<StationListItemResponse>>> getStations() {
        List<StationListItemResponse> stations = shopService.searchAllStations();
        ApiResponse<List<StationListItemResponse>> response = ApiResponse.success(stations);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "음식종류 목록 조회", description = "음식종류 전체 목록을 조회합니다. 코드와 표시명을 반환합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/food-types")
    public ResponseEntity<ApiResponse<List<FoodTypeListItemResponse>>> getFoodTypes() {
        List<FoodTypeListItemResponse> foodTypes = shopService.searchAllFoodTypes();
        ApiResponse<List<FoodTypeListItemResponse>> response = ApiResponse.success(foodTypes);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "편의시설 목록 조회", description = "편의시설 전체 목록을 조회합니다. 코드와 표시명을 반환합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/amenities")
    public ResponseEntity<ApiResponse<List<AmenityListItemResponse>>> getAmenities() {
        List<AmenityListItemResponse> amenities = shopService.searchAllAmenities();
        ApiResponse<List<AmenityListItemResponse>> response = ApiResponse.success(amenities);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "가게 상세 조회", description = "가게의 기본 정보를 조회합니다. 상호명, 주소, 위도/경도, 평점, 전화번호, 썸네일 이미지를 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                   @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 가게")})
    @GetMapping("/v1/{shopId}")
    public ResponseEntity<ApiResponse<ShopDetailResponse>> getShopDetail(@PathVariable Long shopId) {
        ShopDetailResponse shopDetail = shopService.getShopDetail(shopId);
        return ResponseEntity.ok(ApiResponse.success(shopDetail));
    }

    @Operation(summary = "정보 조회", description = "가게의 기본 정보를 조회합니다. 운영시간, 전화번호 등을 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{shopId}/info")
    public ResponseEntity<ApiResponse<ShopInfoResponse>> getShopInfo(@PathVariable Long shopId) {
        ShopInfoResponse shopInfo = shopService.getShopInfo(shopId);
        ApiResponse<ShopInfoResponse> response = ApiResponse.success(shopInfo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "배너 이미지 조회", description = "가게의 배너 이미지 목록을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{shopId}/banners")
    public ResponseEntity<ApiResponse<List<ShopBannerResponse>>> getShopBanners(@PathVariable Long shopId) {
        List<ShopBannerResponse> banners = shopService.getShopBanners(shopId);
        ApiResponse<List<ShopBannerResponse>> response = ApiResponse.success(banners);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 목록 조회", description = "가게의 상품 목록을 조회합니다. 카테고리별로 그룹화되어 반환됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{shopId}/products")
    public ResponseEntity<ApiResponse<List<ShopProductCategoryResponse>>> getShopProducts(@PathVariable Long shopId) {
        List<ShopProductCategoryResponse> products = shopService.getShopProducts(shopId);
        ApiResponse<List<ShopProductCategoryResponse>> response = ApiResponse.success(products);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포토 목록 조회", description = "가게의 사진 목록을 조회합니다. 카테고리별로 그룹화되어 반환됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{shopId}/photos")
    public ResponseEntity<ApiResponse<List<ShopPhotoCategoryResponse>>> getShopPhotos(@PathVariable Long shopId) {
        List<ShopPhotoCategoryResponse> photos = shopService.getShopPhotos(shopId);
        ApiResponse<List<ShopPhotoCategoryResponse>> response = ApiResponse.success(photos);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 목록 조회", description = "가게의 리뷰 목록을 평점별로 조회합니다. 각 평점(1점~5점)별로 최대 5개씩, 전체 리뷰는 페이지네이션으로 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{shopId}/reviews")
    public ResponseEntity<ApiResponse<ShopReviewsByRatingResponse>> getShopReviews(
        @PathVariable Long shopId,
        @Valid @ModelAttribute PageRequest pageRequest,
        @Parameter(description = "이미지 유무 필터: 미지정=전체, true=이미지 있는 리뷰, false=이미지 없는 리뷰")
        @RequestParam(required = false) Boolean hasImage
    ) {
        ShopReviewsByRatingWithPagination result = shopService.getShopReviewsByRatingWithPagination(shopId, pageRequest.page(), pageRequest.size(), hasImage);
        ApiResponse<ShopReviewsByRatingResponse> response = ApiResponse.success(result.response());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 통계 조회", description = "가게의 리뷰 통계를 조회합니다. 평점, 카테고리별 점수, 재방문의사 등을 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{shopId}/reviews/statistics")
    public ResponseEntity<ApiResponse<ShopReviewStatisticsResponse>> getShopReviewStatistics(@PathVariable Long shopId) {
        ShopReviewStatisticsResponse statistics = shopService.getShopReviewStatistics(shopId);
        ApiResponse<ShopReviewStatisticsResponse> response = ApiResponse.success(statistics);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "북마크 여부 조회", description = "가게가 현재 사용자에 의해 북마크되었는지 여부를 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")})
    @GetMapping("/v1/{shopId}/bookmark")
    public ResponseEntity<ApiResponse<ShopBookmarkResponse>> isBookmarked(
        @PathVariable Long shopId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        ShopBookmarkResponse bookmarked;
        if (userDetails == null) {
            bookmarked = ShopBookmarkResponse.from(false);
        } else {
            Long memberId = userDetails.getMemberId();
            bookmarked = shopService.isBookmarked(shopId, memberId);
        }
        return ResponseEntity.ok(ApiResponse.success(bookmarked));
    }

    @Operation(summary = "북마크 토글", description = "가게에 대한 북마크를 추가하거나 제거합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공")})
    @PostMapping("/v1/{shopId}/bookmark")
    public ResponseEntity<ApiResponse<ShopBookmarkResponse>> toggleBookmark(
        @PathVariable Long shopId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        boolean bookmarked = shopService.toggleBookmark(shopId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(ShopBookmarkResponse.from(bookmarked)));
    }

    @Operation(summary = "주문 수단 조회", description = "가게에서 주문 가능한 수단을 조회합니다. 테이블 오더, 예약, 포장 정보를 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{shopId}/order-methods")
    public ResponseEntity<ApiResponse<ShopOrderMethodResponse>> getShopOrderMethods(@PathVariable Long shopId) {
        ShopOrderMethodResponse orderMethods = shopService.getShopOrderMethods(shopId);
        ApiResponse<ShopOrderMethodResponse> response = ApiResponse.success(orderMethods);
        return ResponseEntity.ok(response);
    }
}
