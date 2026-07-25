package com.tastyhouse.ceoapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.ceoapi.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.response.ShopHygieneBadgeResponse;

@Tag(name = "Ceo Shop Hygiene Badge", description = "점주 가게 위생 인증 뱃지 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopHygieneBadgeApiController {

    private final ShopHygieneBadgeService shopHygieneBadgeService;

    @Operation(summary = "위생 인증 뱃지 목록 조회", description = "가게의 위생 인증 뱃지 목록을 조회합니다.")
    @GetMapping("/v1/{id}/hygiene-badges")
    public ResponseEntity<ApiResponse<List<ShopHygieneBadgeResponse>>> getHygieneBadges(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopHygieneBadgeResponse> response = shopHygieneBadgeService.getHygieneBadges(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
