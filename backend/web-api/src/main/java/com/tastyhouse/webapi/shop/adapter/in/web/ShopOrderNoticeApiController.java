package com.tastyhouse.webapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeQueryUseCase;
import com.tastyhouse.webapi.shop.adapter.in.web.response.ShopOrderNoticeResponse;

@Tag(name = "Shop Order Notice", description = "가게 주문안내 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOrderNoticeApiController {
    private final ShopOrderNoticeQueryUseCase shopOrderNoticeQueryService;

    public ShopOrderNoticeApiController(ShopOrderNoticeQueryUseCase shopOrderNoticeQueryService) {
        this.shopOrderNoticeQueryService = shopOrderNoticeQueryService;
    }

    @Operation(summary = "주문안내 조회", description = "가게의 주문안내를 조회합니다. 미설정이거나 관리자 게시중단 상태면 data가 null입니다.")
    @GetMapping("/v1/{id}/order-notice")
    public ResponseEntity<ApiResponse<ShopOrderNoticeResponse>> getOrderNotice(@PathVariable Long id) {
        ShopOrderNoticeResult result = shopOrderNoticeQueryService.getOrderNotice(id);
        ShopOrderNoticeResponse response = result == null ? null : ShopOrderNoticeResponse.from(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
