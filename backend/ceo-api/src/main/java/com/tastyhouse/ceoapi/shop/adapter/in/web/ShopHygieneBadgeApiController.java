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

import com.tastyhouse.ceoapplication.shop.port.in.ShopHygieneBadgeOwnerQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopHygieneBadgeResponse;
import com.tastyhouse.ceoapplication.auth.security.CeoUserDetails;

@Tag(name = "Ceo Shop Hygiene Badge", description = "점주 가게 위생 인증 뱃지 조회 API")
@RestController
@RequestMapping("/api/shops")
public class ShopHygieneBadgeApiController {

    private final ShopHygieneBadgeOwnerQueryUseCase shopHygieneBadgeQueryService;

    public ShopHygieneBadgeApiController(ShopHygieneBadgeOwnerQueryUseCase shopHygieneBadgeQueryService) {
        this.shopHygieneBadgeQueryService = shopHygieneBadgeQueryService;
    }

    @Operation(summary = "위생 인증 뱃지 목록 조회", description = "가게의 위생 인증 뱃지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/hygiene-badges")
    public ResponseEntity<ApiResponse<List<ShopHygieneBadgeResponse>>> getHygieneBadges(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopHygieneBadgeResponse> response = shopHygieneBadgeQueryService.getHygieneBadges(userDetails.getCeoId(), id).stream()
            .map(ShopHygieneBadgeResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
