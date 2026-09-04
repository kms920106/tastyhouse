package com.tastyhouse.adminapi.shop.adapter.in.web;

import com.tastyhouse.adminapplication.shop.port.in.ShopHygieneBadgeCommandUseCase;
import com.tastyhouse.adminapplication.shop.port.in.ShopHygieneBadgeCreateCommand;
import com.tastyhouse.adminapplication.shop.port.in.ShopHygieneBadgeDeleteCommand;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopHygieneBadgeCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopHygieneBadgeResponse;
import com.tastyhouse.adminapplication.shop.port.in.ShopHygieneBadgeManagementQueryUseCase;

@Tag(name = "Shop Hygiene Badge Admin", description = "가게 위생 인증 뱃지 등록 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopHygieneBadgeAdminApiController {

    private final ShopHygieneBadgeManagementQueryUseCase shopHygieneBadgeQueryUseCase;
    private final ShopHygieneBadgeCommandUseCase shopHygieneBadgeCommandUseCase;

    public ShopHygieneBadgeAdminApiController(ShopHygieneBadgeManagementQueryUseCase shopHygieneBadgeQueryUseCase, ShopHygieneBadgeCommandUseCase shopHygieneBadgeCommandUseCase) {
        this.shopHygieneBadgeQueryUseCase = shopHygieneBadgeQueryUseCase;
        this.shopHygieneBadgeCommandUseCase = shopHygieneBadgeCommandUseCase;
    }

    @Operation(summary = "위생 인증 뱃지 목록 조회", description = "가게의 위생 인증 뱃지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/hygiene-badges")
    public ResponseEntity<ApiResponse<List<ShopHygieneBadgeResponse>>> getHygieneBadges(@PathVariable Long id) {
        List<ShopHygieneBadgeResponse> response = shopHygieneBadgeQueryUseCase.getHygieneBadges(id).stream()
            .map(ShopHygieneBadgeResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "위생 인증 뱃지 등록", description = "가게에 위생 인증 뱃지를 등록합니다. 생성된 뱃지 ID를 반환합니다.")
    @PostMapping("/v1/{id}/hygiene-badges")
    public ResponseEntity<ApiResponse<Long>> createHygieneBadge(
        @PathVariable Long id,
        @Valid @RequestBody ShopHygieneBadgeCreateRequest request
    ) {
        ShopHygieneBadgeCreateCommand command = request.toCommand(id);
        Long hygieneBadgeId = shopHygieneBadgeCommandUseCase.createHygieneBadge(command);
        return ResponseEntity.ok(ApiResponse.success(hygieneBadgeId));
    }

    @Operation(summary = "위생 인증 뱃지 삭제", description = "등록된 위생 인증 뱃지를 삭제합니다.")
    @DeleteMapping("/v1/hygiene-badges/{hygieneBadgeId}")
    public ResponseEntity<ApiResponse<Void>> deleteHygieneBadge(@PathVariable Long hygieneBadgeId) {
        ShopHygieneBadgeDeleteCommand command = ShopHygieneBadgeDeleteCommand.of(hygieneBadgeId);
        shopHygieneBadgeCommandUseCase.deleteHygieneBadge(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
