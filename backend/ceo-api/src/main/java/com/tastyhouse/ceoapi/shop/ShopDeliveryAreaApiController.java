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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopDeliveryAreaCreateRequest;
import com.tastyhouse.ceoapi.shop.response.ShopDeliveryAreaItemResponse;

@Tag(name = "Ceo Shop Delivery Area", description = "점주 가게 배달가능지역 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopDeliveryAreaApiController {

    private final ShopDeliveryAreaQueryService shopDeliveryAreaQueryService;
    private final ShopDeliveryAreaCommandService shopDeliveryAreaCommandService;

    public ShopDeliveryAreaApiController(ShopDeliveryAreaQueryService shopDeliveryAreaQueryService, ShopDeliveryAreaCommandService shopDeliveryAreaCommandService) {
        this.shopDeliveryAreaQueryService = shopDeliveryAreaQueryService;
        this.shopDeliveryAreaCommandService = shopDeliveryAreaCommandService;
    }

    @Operation(summary = "내 가게 배달가능지역 조회", description = "로그인한 점주가 소유한 가게의 배달가능지역(행정동) 목록을 조회합니다.")
    @GetMapping("/v1/{id}/delivery-areas")
    public ResponseEntity<ApiResponse<List<ShopDeliveryAreaItemResponse>>> getDeliveryAreas(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopDeliveryAreaItemResponse> response = shopDeliveryAreaQueryService.getDeliveryAreas(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 배달가능지역 추가", description = "로그인한 점주가 소유한 가게에 배달가능지역(행정동)을 추가합니다.")
    @PostMapping("/v1/{id}/delivery-areas")
    public ResponseEntity<ApiResponse<Long>> createDeliveryArea(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaCreateRequest request
    ) {
        Long deliveryAreaId = shopDeliveryAreaCommandService.addDeliveryArea(userDetails.getCeoId(), id, request.adminDongId());
        return ResponseEntity.ok(ApiResponse.success(deliveryAreaId));
    }

    @Operation(summary = "내 가게 배달가능지역 삭제", description = "로그인한 점주가 소유한 가게의 배달가능지역을 삭제합니다. 해당 지역에 지역별 배달팁이 설정돼 있으면 삭제할 수 없습니다.")
    @DeleteMapping("/v1/delivery-areas/{deliveryAreaId}")
    public ResponseEntity<ApiResponse<Void>> deleteDeliveryArea(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long deliveryAreaId
    ) {
        shopDeliveryAreaCommandService.removeDeliveryArea(userDetails.getCeoId(), deliveryAreaId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
