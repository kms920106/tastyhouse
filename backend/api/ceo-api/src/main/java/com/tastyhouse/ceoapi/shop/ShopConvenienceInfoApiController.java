package com.tastyhouse.ceoapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopAmenityAssignRequest;
import com.tastyhouse.ceoapi.shop.request.ShopConvenienceInfoUpdateRequest;
import com.tastyhouse.ceoapi.shop.response.ShopAmenityResponse;
import com.tastyhouse.ceoapi.shop.response.ShopConvenienceInfoResponse;

@Tag(name = "Ceo Shop Convenience Info", description = "점주 가게 편의정보·편의시설 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopConvenienceInfoApiController {

    private final ShopConvenienceInfoQueryService shopConvenienceInfoQueryService;
    private final ShopConvenienceInfoCommandService shopConvenienceInfoCommandService;

    public ShopConvenienceInfoApiController(ShopConvenienceInfoQueryService shopConvenienceInfoQueryService, ShopConvenienceInfoCommandService shopConvenienceInfoCommandService) {
        this.shopConvenienceInfoQueryService = shopConvenienceInfoQueryService;
        this.shopConvenienceInfoCommandService = shopConvenienceInfoCommandService;
    }

    @Operation(summary = "내 가게 편의정보 조회", description = "로그인한 점주가 소유한 가게의 편의정보(주차·발렛·찾아오는길·노출위치)를 조회합니다.")
    @GetMapping("/v1/{id}/convenience-info")
    public ResponseEntity<ApiResponse<ShopConvenienceInfoResponse>> getConvenienceInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopConvenienceInfoResponse response = shopConvenienceInfoQueryService.getConvenienceInfo(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 편의정보 등록/수정", description = "로그인한 점주가 소유한 가게의 편의정보를 등록하거나 수정합니다. 노출 위치는 가게 실제 위치 기준 1km 이내여야 합니다.")
    @PutMapping("/v1/{id}/convenience-info")
    public ResponseEntity<ApiResponse<Void>> updateConvenienceInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopConvenienceInfoUpdateRequest request
    ) {
        shopConvenienceInfoCommandService.updateConvenienceInfo(
            userDetails.getCeoId(),
            id,
            request.parkingAvailable(),
            request.parkingPaid(),
            request.valetAvailable(),
            request.valetPaid(),
            request.directionsGuide(),
            request.displayLatitude(),
            request.displayLongitude()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 편의시설 목록 조회", description = "로그인한 점주가 소유한 가게에 지정된 편의시설 목록을 조회합니다.")
    @GetMapping("/v1/{id}/amenities")
    public ResponseEntity<ApiResponse<List<ShopAmenityResponse>>> getAmenities(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopAmenityResponse> response = shopConvenienceInfoQueryService.getAmenities(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 편의시설 지정", description = "로그인한 점주가 소유한 가게에 편의시설을 지정합니다.")
    @PostMapping("/v1/{id}/amenities")
    public ResponseEntity<ApiResponse<Long>> assignAmenity(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopAmenityAssignRequest request
    ) {
        Long amenityId = shopConvenienceInfoCommandService.assignAmenity(userDetails.getCeoId(), id, request.amenityCategoryId());
        return ResponseEntity.ok(ApiResponse.success(amenityId));
    }

    @Operation(summary = "내 가게 편의시설 해제", description = "로그인한 점주가 소유한 가게에 지정된 편의시설을 해제합니다.")
    @DeleteMapping("/v1/{id}/amenities/{amenityCategoryId}")
    public ResponseEntity<ApiResponse<Void>> unassignAmenity(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long amenityCategoryId
    ) {
        shopConvenienceInfoCommandService.unassignAmenity(userDetails.getCeoId(), id, amenityCategoryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
