package com.tastyhouse.ceoapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.in.ShopSuspensionQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopSuspensionBulkCreateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopSuspensionCreateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopSuspensionResponse;
import com.tastyhouse.application.shop.port.in.ShopSuspensionBulkCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopSuspensionCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopSuspensionCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopSuspensionReleaseCommand;

@Tag(name = "Ceo Shop Suspension", description = "점주 가게 영업 임시중지 API")
@RestController
@RequestMapping("/api/shops")
public class ShopSuspensionApiController {

    private final ShopSuspensionQueryUseCase shopSuspensionQueryService;
    private final ShopSuspensionCommandUseCase shopSuspensionCommandUseCase;

    public ShopSuspensionApiController(ShopSuspensionQueryUseCase shopSuspensionQueryService, ShopSuspensionCommandUseCase shopSuspensionCommandUseCase) {
        this.shopSuspensionQueryService = shopSuspensionQueryService;
        this.shopSuspensionCommandUseCase = shopSuspensionCommandUseCase;
    }

    @Operation(summary = "영업 임시중지 목록 조회", description = "가게의 영업 임시중지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/suspensions")
    public ResponseEntity<ApiResponse<List<ShopSuspensionResponse>>> getSuspensions(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopSuspensionResponse> response = shopSuspensionQueryService.getSuspensions(userDetails.getCeoId(), id).stream()
            .map(ShopSuspensionResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "영업 임시중지 등록", description = "가게에 영업 임시중지를 등록합니다. 주문수단을 비우면 전체 주문수단 대상으로 1건 생성되고, 지정하면 주문수단별로 각각 생성됩니다.")
    @PostMapping("/v1/{id}/suspensions")
    public ResponseEntity<ApiResponse<List<Long>>> createSuspension(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopSuspensionCreateRequest request
    ) {
        ShopSuspensionCreateCommand command = request.toCommand(userDetails.getCeoId(), id);
        List<Long> suspensionIds = shopSuspensionCommandUseCase.createSuspension(command);
        return ResponseEntity.ok(ApiResponse.success(suspensionIds));
    }

    @Operation(summary = "영업 임시중지 해제", description = "가게의 영업 임시중지를 즉시 해제합니다.")
    @PatchMapping("/v1/{id}/suspensions/{suspensionId}/release")
    public ResponseEntity<ApiResponse<Void>> releaseSuspension(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long suspensionId
    ) {
        ShopSuspensionReleaseCommand command = ShopSuspensionReleaseCommand.of(userDetails.getCeoId(), id, suspensionId);
        shopSuspensionCommandUseCase.releaseSuspension(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "영업 임시중지 일괄 등록", description = "여러 가게에 동일한 사유/기간으로 영업 임시중지를 일괄 등록합니다.")
    @PostMapping("/v1/suspensions/bulk")
    public ResponseEntity<ApiResponse<List<Long>>> createSuspensionsBulk(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody ShopSuspensionBulkCreateRequest request
    ) {
        ShopSuspensionBulkCreateCommand command = request.toCommand(userDetails.getCeoId());
        List<Long> suspensionIds = shopSuspensionCommandUseCase.createSuspensionsBulk(command);
        return ResponseEntity.ok(ApiResponse.success(suspensionIds));
    }
}
