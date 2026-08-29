package com.tastyhouse.ceoapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.ceoapi.shop.application.port.in.ShopOrderAvailabilityQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopOrderAvailabilityResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopOrderMethodItemResponse;

@Tag(name = "Ceo Shop Order Availability", description = "점주 가게 주문가능 상태 조회 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOrderAvailabilityApiController {

    private final ShopOrderAvailabilityQueryUseCase shopOrderAvailabilityQueryService;

    public ShopOrderAvailabilityApiController(ShopOrderAvailabilityQueryUseCase shopOrderAvailabilityQueryService) {
        this.shopOrderAvailabilityQueryService = shopOrderAvailabilityQueryService;
    }

    @Operation(
        summary = "내 가게 주문가능 상태 조회",
        description = "로그인한 점주가 소유한 가게의 주문가능 상태와, 배정된 주문유형별 주문가능 상태를 함께 조회합니다. "
            + "주문 방식별로 임시중지를 걸면 그 유형만 불가가 되고 가게 전체 상태는 유지됩니다. "
            + "주문유형 배정이 없으면 orderMethods는 빈 배열입니다."
    )
    @GetMapping("/v1/{id}/order-availability")
    public ResponseEntity<ApiResponse<ShopOrderAvailabilityResponse>> getOrderAvailability(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopOrderAvailabilityResponse response =
            shopOrderAvailabilityQueryService.getOrderAvailability(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "내 가게 주문유형 배정 조회",
        description = "로그인한 점주가 소유한 가게에 배정된 주문유형 목록을 조회합니다. 배정 변경은 관리자 권한 영역입니다."
    )
    @GetMapping("/v1/{id}/order-methods")
    public ResponseEntity<ApiResponse<List<ShopOrderMethodItemResponse>>> getOrderMethods(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopOrderMethodItemResponse> response =
            shopOrderAvailabilityQueryService.getOrderMethods(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
