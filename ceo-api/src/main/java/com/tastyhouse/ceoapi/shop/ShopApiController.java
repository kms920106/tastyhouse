package com.tastyhouse.ceoapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopSearchRequest;
import com.tastyhouse.ceoapi.shop.response.ShopDetailResponse;
import com.tastyhouse.ceoapi.shop.response.ShopListItemResponse;

@Tag(name = "Ceo Shop", description = "점주 가게 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopApiController {

    private final ShopQueryService shopQueryService;

    @Operation(summary = "내 가게 목록 조회", description = "로그인한 점주가 소유한 가게 목록을 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ShopListItemResponse>>> getMyShops(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @ModelAttribute ShopSearchRequest request,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ShopListItemResponse> response = shopQueryService.getMyShops(
            userDetails.getCeoId(),
            request.name(),
            request.stationId(),
            request.permanentlyClosed(),
            pageRequest.page(),
            pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(
            response.content(),
            response.page(),
            response.size(),
            response.totalElements()
        ));
    }

    @Operation(summary = "내 가게 상세 조회", description = "로그인한 점주가 소유한 가게의 상세 정보를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ShopDetailResponse>> getMyShop(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopDetailResponse response = shopQueryService.getMyShop(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
