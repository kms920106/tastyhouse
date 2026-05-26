package com.tastyhouse.webapi.place;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.core.domain.place.domain.model.Amenity;
import com.tastyhouse.core.domain.place.domain.model.FoodType;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.place.request.LatestPlaceFilterRequest;
import com.tastyhouse.webapi.place.response.AmenityListItemResponse;
import com.tastyhouse.webapi.place.response.BestPlaceListItemResponse;
import com.tastyhouse.webapi.place.response.EditorChoiceResponse;
import com.tastyhouse.webapi.place.response.FoodTypeListItemResponse;
import com.tastyhouse.webapi.place.response.LatestPlaceListItemResponse;
import com.tastyhouse.webapi.place.response.PlaceBannerResponse;
import com.tastyhouse.webapi.place.response.PlaceBookmarkResponse;
import com.tastyhouse.webapi.place.response.PlaceInfoResponse;
import com.tastyhouse.webapi.place.response.PlaceMapMarkerResponse;
import com.tastyhouse.webapi.place.response.PlaceProductCategoryResponse;
import com.tastyhouse.webapi.place.response.PlaceOrderMethodResponse;
import com.tastyhouse.webapi.place.response.PlacePhotoCategoryResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewStatisticsResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewsByRatingResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewsByRatingWithPagination;
import com.tastyhouse.webapi.place.response.PlaceDetailResponse;
import com.tastyhouse.webapi.place.response.StationListItemResponse;
import com.tastyhouse.webapi.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.tastyhouse.webapi.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Tag(name = "Place", description = "플레이스 관리 API")
public class PlaceApiController {

    private final PlaceService placeService;

    @Operation(summary = "지도 마커 목록 조회", description = "지도에서 드래그한 위치 기준 주변 플레이스의 마커 정보(위도, 경도, 상호명)를 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/map/markers")
    public ResponseEntity<ApiResponse<List<PlaceMapMarkerResponse>>> getMapMarkers(
            @RequestParam @Parameter(description = "위도", example = "37.5013") Double latitude,
            @RequestParam @Parameter(description = "경도", example = "127.0396") Double longitude) {
        List<PlaceMapMarkerResponse> markers = placeService.searchMapMarkers(latitude, longitude);
        ApiResponse<List<PlaceMapMarkerResponse>> response = ApiResponse.success(markers);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "베스트 플레이스 목록 조회", description = "평점 기준 베스트 플레이스를 페이징하여 조회합니다. 이미지, 전철역명, 평점, 가게명, 태그 정보를 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/best")
    public ResponseEntity<ApiResponse<List<BestPlaceListItemResponse>>> getBestPlaces(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResponse<BestPlaceListItemResponse> pageResult = placeService.searchBestPlaces(pageRequest.page(), pageRequest.size());
        ApiResponse<List<BestPlaceListItemResponse>> response = ApiResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "테하 초이스 조회", description = "특정 테하 초이스의 가게 이미지, 제목, 내용, 관련 상품 목록을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/editor-choice")
    public ResponseEntity<ApiResponse<List<EditorChoiceResponse>>> getEditorChoices(@Valid @ModelAttribute PageRequest pageRequest) {
        List<EditorChoiceResponse> editorChoiceResponses = placeService.searchEditorChoices(pageRequest.page(), pageRequest.size());
        ApiResponse<List<EditorChoiceResponse>> response = ApiResponse.success(editorChoiceResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "최신 플레이스 목록 조회", description = "최근 등록된 플레이스를 페이징하여 조회합니다. 이미지, 전철역명, 평점, 가게명, 태그, 등록일 정보를 포함합니다. 전철역, 음식종류, 편의시설 필터를 적용할 수 있습니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/latest")
    public ResponseEntity<ApiResponse<List<LatestPlaceListItemResponse>>> getLatestPlaces(
            @Valid @ModelAttribute PageRequest pageRequest,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) List<FoodType> foodTypes,
            @RequestParam(required = false) List<Amenity> amenities) {
        LatestPlaceFilterRequest filterRequest = new LatestPlaceFilterRequest(stationId, foodTypes, amenities);
        PageResponse<LatestPlaceListItemResponse> pageResult = placeService.searchLatestPlaces(filterRequest, pageRequest.page(), pageRequest.size());
        ApiResponse<List<LatestPlaceListItemResponse>> response = ApiResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전철역 목록 조회", description = "전철역 목록을 가나다라 순으로 조회합니다. ID와 전철역명을 반환합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/stations")
    public ResponseEntity<ApiResponse<List<StationListItemResponse>>> getStations() {
        List<StationListItemResponse> stations = placeService.searchAllStations();
        ApiResponse<List<StationListItemResponse>> response = ApiResponse.success(stations);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "음식종류 목록 조회", description = "음식종류 전체 목록을 조회합니다. 코드와 표시명을 반환합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/food-types")
    public ResponseEntity<ApiResponse<List<FoodTypeListItemResponse>>> getFoodTypes() {
        List<FoodTypeListItemResponse> foodTypes = placeService.searchAllFoodTypes();
        ApiResponse<List<FoodTypeListItemResponse>> response = ApiResponse.success(foodTypes);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "편의시설 목록 조회", description = "편의시설 전체 목록을 조회합니다. 코드와 표시명을 반환합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/amenities")
    public ResponseEntity<ApiResponse<List<AmenityListItemResponse>>> getAmenities() {
        List<AmenityListItemResponse> amenities = placeService.searchAllAmenities();
        ApiResponse<List<AmenityListItemResponse>> response = ApiResponse.success(amenities);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "플레이스 상세 조회", description = "플레이스의 기본 정보를 조회합니다. 상호명, 주소, 위도/경도, 평점, 전화번호, 썸네일 이미지를 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                   @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 플레이스")})
    @GetMapping("/v1/{placeId}")
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlaceDetail(@PathVariable Long placeId) {
        PlaceDetailResponse placeDetail = placeService.getPlaceDetail(placeId);
        return ResponseEntity.ok(ApiResponse.success(placeDetail));
    }

    @Operation(summary = "정보 조회", description = "플레이스의 기본 정보를 조회합니다. 운영시간, 전화번호 등을 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{placeId}/info")
    public ResponseEntity<ApiResponse<PlaceInfoResponse>> getPlaceInfo(@PathVariable Long placeId) {
        PlaceInfoResponse placeInfo = placeService.getPlaceInfo(placeId);
        ApiResponse<PlaceInfoResponse> response = ApiResponse.success(placeInfo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "배너 이미지 조회", description = "플레이스의 배너 이미지 목록을 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{placeId}/banners")
    public ResponseEntity<ApiResponse<List<PlaceBannerResponse>>> getPlaceBanners(@PathVariable Long placeId) {
        List<PlaceBannerResponse> banners = placeService.getPlaceBanners(placeId);
        ApiResponse<List<PlaceBannerResponse>> response = ApiResponse.success(banners);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "상품 목록 조회", description = "플레이스의 상품 목록을 조회합니다. 카테고리별로 그룹화되어 반환됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{placeId}/products")
    public ResponseEntity<ApiResponse<List<PlaceProductCategoryResponse>>> getPlaceProducts(@PathVariable Long placeId) {
        List<PlaceProductCategoryResponse> products = placeService.getPlaceProducts(placeId);
        ApiResponse<List<PlaceProductCategoryResponse>> response = ApiResponse.success(products);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포토 목록 조회", description = "플레이스의 사진 목록을 조회합니다. 카테고리별로 그룹화되어 반환됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{placeId}/photos")
    public ResponseEntity<ApiResponse<List<PlacePhotoCategoryResponse>>> getPlacePhotos(@PathVariable Long placeId) {
        List<PlacePhotoCategoryResponse> photos = placeService.getPlacePhotos(placeId);
        ApiResponse<List<PlacePhotoCategoryResponse>> response = ApiResponse.success(photos);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 목록 조회", description = "플레이스의 리뷰 목록을 평점별로 조회합니다. 각 평점(1점, 2점, 3점, 4점, 5점)별로 최대 5개씩, 전체 리뷰는 페이지네이션으로 조회합니다. 총 리뷰 개수도 함께 반환됩니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{placeId}/reviews")
    public ResponseEntity<ApiResponse<PlaceReviewsByRatingResponse>> getPlaceReviews(
            @PathVariable Long placeId,
            @Valid @ModelAttribute PageRequest pageRequest) {
        PlaceReviewsByRatingWithPagination result = placeService.getPlaceReviewsByRatingWithPagination(placeId, pageRequest.page(), pageRequest.size());
        ApiResponse<PlaceReviewsByRatingResponse> response = ApiResponse.success(result.response());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 통계 조회", description = "플레이스의 리뷰 통계를 조회합니다. 평점, 카테고리별 점수, 재방문의사 등을 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{placeId}/reviews/statistics")
    public ResponseEntity<ApiResponse<PlaceReviewStatisticsResponse>> getPlaceReviewStatistics(@PathVariable Long placeId) {
        PlaceReviewStatisticsResponse statistics = placeService.getPlaceReviewStatistics(placeId);
        ApiResponse<PlaceReviewStatisticsResponse> response = ApiResponse.success(statistics);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "북마크 여부 조회", description = "플레이스가 현재 사용자에 의해 북마크되었는지 여부를 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")})
    @GetMapping("/v1/{placeId}/bookmark")
    public ResponseEntity<ApiResponse<PlaceBookmarkResponse>> isBookmarked(@PathVariable Long placeId, @CurrentUser CustomUserDetails userDetails) {
        PlaceBookmarkResponse bookmarked;
        if (userDetails == null) {
            bookmarked = PlaceBookmarkResponse.from(false);
        } else {
            Long memberId = userDetails.getMemberId();
            bookmarked = placeService.isBookmarked(placeId, memberId);
        }
        return ResponseEntity.ok(ApiResponse.success(bookmarked));
    }

    @Operation(summary = "북마크 토글", description = "플레이스에 대한 북마크를 추가하거나 제거합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공")})
    @PostMapping("/v1/{placeId}/bookmark")
    public ResponseEntity<ApiResponse<PlaceBookmarkResponse>> toggleBookmark(@PathVariable Long placeId, @CurrentUser CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        boolean bookmarked = placeService.toggleBookmark(placeId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(PlaceBookmarkResponse.from(bookmarked)));
    }

    @Operation(summary = "주문 수단 조회", description = "플레이스에서 주문 가능한 수단을 조회합니다. 테이블 오더, 예약, 포장 정보를 포함합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{placeId}/order-methods")
    public ResponseEntity<ApiResponse<PlaceOrderMethodResponse>> getPlaceOrderMethods(@PathVariable Long placeId) {
        PlaceOrderMethodResponse orderMethods = placeService.getPlaceOrderMethods(placeId);
        ApiResponse<PlaceOrderMethodResponse> response = ApiResponse.success(orderMethods);
        return ResponseEntity.ok(response);
    }
}
