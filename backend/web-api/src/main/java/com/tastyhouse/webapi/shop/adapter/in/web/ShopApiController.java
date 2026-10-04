package com.tastyhouse.webapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.shop.port.in.ShopSearchQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.shop.adapter.in.web.request.ShopMapMarkerSearchRequest;
import com.tastyhouse.webapi.shop.adapter.in.web.request.ShopSearchRequest;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopAmenityListItemResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopBestListItemResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopEditorChoiceResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopFoodTypeListItemResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopLatestListItemResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopMapMarkerResponse;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopStationListItemResponse;

@RestController
@RequestMapping("/api/shops")
@Tag(name = "Shop", description = "가게 관리 API")
class ShopApiController {

    private final ShopSearchQueryUseCase shopSearchQueryUseCase;

    public ShopApiController(ShopSearchQueryUseCase shopSearchQueryUseCase) {
        this.shopSearchQueryUseCase = shopSearchQueryUseCase;
    }

    @Operation(summary = "지도 마커 목록 조회", description = "지도에서 드래그한 위치 기준 주변 가게의 마커 정보(위도, 경도, 상호명)를 조회합니다.")
    @GetMapping("/v1/map/markers")
    public ResponseEntity<ApiResponse<List<ShopMapMarkerResponse>>> getMapMarkers(
        @Valid @ModelAttribute ShopMapMarkerSearchRequest search
    ) {
        List<ShopMapMarkerResponse> markers = shopSearchQueryUseCase
            .searchMapMarkers(search.latitude(), search.longitude()).stream()
            .map(ShopMapMarkerResponse::from)
            .toList();
        ApiResponse<List<ShopMapMarkerResponse>> response = ApiResponse.success(markers);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "베스트 가게 목록 조회", description = "평점 기준 베스트 가게를 페이징하여 조회합니다. 이미지, 지하철역명, 평점, 가게명, 태그 정보를 포함합니다.")
    @GetMapping("/v1/best")
    public ResponseEntity<ApiResponse<List<ShopBestListItemResponse>>> getBestShops(
        @Valid @ModelAttribute PageRequest pageRequest,
        @CurrentUser MemberUserDetails userDetails
    ) {
        PaginationResponse<ShopBestListItemResponse> pageResponse = PaginationResponse.from(
            shopSearchQueryUseCase.searchBestShops(memberIdOrNull(userDetails), pageRequest.page(), pageRequest.size())
                .map(ShopBestListItemResponse::from)
        );
        ApiResponse<List<ShopBestListItemResponse>> response = ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "테하 초이스 조회", description = "특정 테하 초이스의 가게 이미지, 제목, 내용, 관련 상품 목록을 조회합니다.")
    @GetMapping("/v1/editor-choice")
    public ResponseEntity<ApiResponse<List<ShopEditorChoiceResponse>>> getEditorChoices(@Valid @ModelAttribute PageRequest pageRequest) {
        List<ShopEditorChoiceResponse> editorChoiceResponses = shopSearchQueryUseCase
            .searchEditorChoices(pageRequest.page(), pageRequest.size()).stream()
            .map(ShopEditorChoiceResponse::from)
            .toList();
        ApiResponse<List<ShopEditorChoiceResponse>> response = ApiResponse.success(editorChoiceResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "최신 가게 목록 조회", description = "최근 등록된 가게를 페이징하여 조회합니다. 이미지, 지하철역명, 평점, 가게명, 태그, 등록일 정보를 포함합니다. 지하철역, 음식종류, 편의시설 필터를 적용할 수 있습니다.")
    @GetMapping("/v1/latest")
    public ResponseEntity<ApiResponse<List<ShopLatestListItemResponse>>> getLatestShops(
        @Valid @ModelAttribute ShopSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest,
        @CurrentUser MemberUserDetails userDetails
    ) {
        PaginationResponse<ShopLatestListItemResponse> pageResponse = PaginationResponse.from(
            shopSearchQueryUseCase.searchLatestShops(
                search.stationId(),
                search.foodTypes(),
                search.amenities(),
                memberIdOrNull(userDetails),
                pageRequest.page(),
                pageRequest.size()
            ).map(ShopLatestListItemResponse::from)
        );
        ApiResponse<List<ShopLatestListItemResponse>> response = ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지하철역 목록 조회", description = "지하철역 목록을 가나다라 순으로 조회합니다. ID와 역명을 반환합니다.")
    @GetMapping("/v1/stations")
    public ResponseEntity<ApiResponse<List<ShopStationListItemResponse>>> getStations() {
        List<ShopStationListItemResponse> stations = shopSearchQueryUseCase.searchAllStations().stream()
            .map(ShopStationListItemResponse::from)
            .toList();
        ApiResponse<List<ShopStationListItemResponse>> response = ApiResponse.success(stations);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "음식종류 목록 조회", description = "음식종류 전체 목록을 조회합니다. 코드와 표시명을 반환합니다.")
    @GetMapping("/v1/food-types")
    public ResponseEntity<ApiResponse<List<ShopFoodTypeListItemResponse>>> getFoodTypes() {
        List<ShopFoodTypeListItemResponse> foodTypes = shopSearchQueryUseCase.searchAllFoodTypes().stream()
            .map(ShopFoodTypeListItemResponse::from)
            .toList();
        ApiResponse<List<ShopFoodTypeListItemResponse>> response = ApiResponse.success(foodTypes);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "편의시설 목록 조회", description = "편의시설 전체 목록을 조회합니다. 코드와 표시명을 반환합니다.")
    @GetMapping("/v1/amenities")
    public ResponseEntity<ApiResponse<List<ShopAmenityListItemResponse>>> getAmenities() {
        List<ShopAmenityListItemResponse> amenities = shopSearchQueryUseCase.searchAllAmenities().stream()
            .map(ShopAmenityListItemResponse::from)
            .toList();
        ApiResponse<List<ShopAmenityListItemResponse>> response = ApiResponse.success(amenities);
        return ResponseEntity.ok(response);
    }

    private Long memberIdOrNull(MemberUserDetails userDetails) {
        return userDetails == null ? null : userDetails.getMemberId();
    }
}
