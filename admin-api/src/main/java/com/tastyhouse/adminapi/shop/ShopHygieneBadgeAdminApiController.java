package com.tastyhouse.adminapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.shop.request.ShopHygieneBadgeCreateRequest;
import com.tastyhouse.adminapi.shop.response.ShopHygieneBadgeResponse;

@Tag(name = "Shop Hygiene Badge Admin", description = "가게 위생 인증 뱃지 등록 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopHygieneBadgeAdminApiController {

    private final ShopHygieneBadgeQueryService shopHygieneBadgeQueryService;
    private final ShopHygieneBadgeCommandService shopHygieneBadgeCommandService;

    @Operation(summary = "위생 인증 뱃지 목록 조회", description = "가게의 위생 인증 뱃지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/hygiene-badges")
    public ResponseEntity<ApiResponse<List<ShopHygieneBadgeResponse>>> getHygieneBadges(@PathVariable Long id) {
        List<ShopHygieneBadgeResponse> response = shopHygieneBadgeQueryService.getHygieneBadges(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "위생 인증 뱃지 등록", description = "가게에 위생 인증 뱃지를 등록합니다. 생성된 뱃지 ID를 반환합니다.")
    @PostMapping("/v1/{id}/hygiene-badges")
    public ResponseEntity<ApiResponse<Long>> createHygieneBadge(
        @PathVariable Long id,
        @Valid @RequestBody ShopHygieneBadgeCreateRequest request
    ) {
        Long hygieneBadgeId = shopHygieneBadgeCommandService.createHygieneBadge(
            id, request.badgeType(), request.certifiedDate(), request.lastInspectionMonth()
        );
        return ResponseEntity.ok(ApiResponse.success(hygieneBadgeId));
    }

    @Operation(summary = "위생 인증 뱃지 삭제", description = "등록된 위생 인증 뱃지를 삭제합니다.")
    @DeleteMapping("/v1/hygiene-badges/{hygieneBadgeId}")
    public ResponseEntity<ApiResponse<Void>> deleteHygieneBadge(@PathVariable Long hygieneBadgeId) {
        shopHygieneBadgeCommandService.deleteHygieneBadge(hygieneBadgeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
