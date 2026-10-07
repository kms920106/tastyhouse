package com.tastyhouse.adminapi.shop.adapter.in.web;

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

import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeCreateUseCase;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopHygieneBadgeManagementQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopHygieneBadgeCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopHygieneBadgeResponse;

@Tag(name = "Shop Hygiene Badge Admin", description = "가게 위생 인증 뱃지 등록 관리자 API")
@RestController
@RequestMapping("/api/shops")
class ShopHygieneBadgeAdminApiController {

    private final ShopHygieneBadgeManagementQueryUseCase shopHygieneBadgeManagementQueryUseCase;
    private final ShopHygieneBadgeCreateUseCase shopHygieneBadgeCreateUseCase;
    private final ShopHygieneBadgeDeleteUseCase shopHygieneBadgeDeleteUseCase;

    public ShopHygieneBadgeAdminApiController(
        ShopHygieneBadgeManagementQueryUseCase shopHygieneBadgeManagementQueryUseCase,
        ShopHygieneBadgeCreateUseCase shopHygieneBadgeCreateUseCase,
        ShopHygieneBadgeDeleteUseCase shopHygieneBadgeDeleteUseCase
    ) {
        this.shopHygieneBadgeManagementQueryUseCase = shopHygieneBadgeManagementQueryUseCase;
        this.shopHygieneBadgeCreateUseCase = shopHygieneBadgeCreateUseCase;
        this.shopHygieneBadgeDeleteUseCase = shopHygieneBadgeDeleteUseCase;
    }

    @Operation(summary = "위생 인증 뱃지 목록 조회", description = "가게의 위생 인증 뱃지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/hygiene-badges")
    public ResponseEntity<ApiResponse<List<ShopHygieneBadgeResponse>>> getHygieneBadges(@PathVariable Long id) {
        List<ShopHygieneBadgeResponse> response = shopHygieneBadgeManagementQueryUseCase.getHygieneBadges(id).stream()
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
        Long hygieneBadgeId = shopHygieneBadgeCreateUseCase.createHygieneBadge(command);
        return ResponseEntity.ok(ApiResponse.success(hygieneBadgeId));
    }

    @Operation(summary = "위생 인증 뱃지 삭제", description = "등록된 위생 인증 뱃지를 삭제합니다.")
    @DeleteMapping("/v1/hygiene-badges/{hygieneBadgeId}")
    public ResponseEntity<ApiResponse<Void>> deleteHygieneBadge(@PathVariable Long hygieneBadgeId) {
        ShopHygieneBadgeDeleteCommand command = ShopHygieneBadgeDeleteCommand.of(hygieneBadgeId);
        shopHygieneBadgeDeleteUseCase.deleteHygieneBadge(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
