package com.tastyhouse.webapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.shop.port.out.ShopOriginInfoResult;
import com.tastyhouse.application.shop.port.in.ShopOriginInfoQueryUseCase;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopOriginInfoResponse;

@Tag(name = "Shop Origin Info", description = "가게 원산지 표시 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOriginInfoApiController {
    private final ShopOriginInfoQueryUseCase shopOriginInfoQueryService;

    public ShopOriginInfoApiController(ShopOriginInfoQueryUseCase shopOriginInfoQueryService) {
        this.shopOriginInfoQueryService = shopOriginInfoQueryService;
    }

    @Operation(summary = "원산지 조회",
        description = "가게의 원산지 표시 정보를 조회합니다. 미설정이면 data가 null이므로 화면은 원산지 "
            + "영역을 감춥니다.")
    @GetMapping("/v1/{id}/origin")
    public ResponseEntity<ApiResponse<ShopOriginInfoResponse>> getOriginInfo(@PathVariable Long id) {
        ShopOriginInfoResult result = shopOriginInfoQueryService.getOriginInfo(id);
        ShopOriginInfoResponse response = result == null ? null : ShopOriginInfoResponse.from(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
