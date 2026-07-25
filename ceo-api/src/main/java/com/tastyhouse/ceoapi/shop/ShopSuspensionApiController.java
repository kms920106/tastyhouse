package com.tastyhouse.ceoapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.ceoapi.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopSuspensionBulkCreateRequest;
import com.tastyhouse.ceoapi.shop.request.ShopSuspensionCreateRequest;
import com.tastyhouse.ceoapi.shop.response.ShopSuspensionResponse;

@Tag(name = "Ceo Shop Suspension", description = "점주 가게 영업 임시중지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopSuspensionApiController {

    private final ShopSuspensionService shopSuspensionService;

    @Operation(summary = "영업 임시중지 목록 조회", description = "가게의 영업 임시중지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/suspensions")
    public ResponseEntity<ApiResponse<List<ShopSuspensionResponse>>> getSuspensions(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopSuspensionResponse> response = shopSuspensionService.getSuspensions(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "영업 임시중지 등록", description = "가게에 영업 임시중지를 등록합니다. 주문수단을 비우면 전체 주문수단 대상으로 1건 생성되고, 지정하면 주문수단별로 각각 생성됩니다.")
    @PostMapping("/v1/{id}/suspensions")
    public ResponseEntity<ApiResponse<List<Long>>> createSuspension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopSuspensionCreateRequest request
    ) {
        List<Long> suspensionIds = shopSuspensionService.createSuspension(
            userDetails.getCeoId(),
            id,
            request.reason(),
            request.orderMethods(),
            request.startAt(),
            request.endAt()
        );
        return ResponseEntity.ok(ApiResponse.success(suspensionIds));
    }

    @Operation(summary = "영업 임시중지 해제", description = "가게의 영업 임시중지를 즉시 해제합니다.")
    @PatchMapping("/v1/{id}/suspensions/{suspensionId}/release")
    public ResponseEntity<ApiResponse<Void>> releaseSuspension(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long suspensionId
    ) {
        shopSuspensionService.releaseSuspension(userDetails.getCeoId(), id, suspensionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "영업 임시중지 일괄 등록", description = "여러 가게에 동일한 사유/기간으로 영업 임시중지를 일괄 등록합니다.")
    @PostMapping("/v1/suspensions/bulk")
    public ResponseEntity<ApiResponse<List<Long>>> createSuspensionsBulk(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ShopSuspensionBulkCreateRequest request
    ) {
        List<Long> suspensionIds = shopSuspensionService.createSuspensionsBulk(
            userDetails.getCeoId(),
            request.shopIds(),
            request.reason(),
            request.orderMethods(),
            request.startAt(),
            request.endAt()
        );
        return ResponseEntity.ok(ApiResponse.success(suspensionIds));
    }
}
