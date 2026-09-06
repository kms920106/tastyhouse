package com.tastyhouse.ceoapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.in.ShopOrderNoticeOwnerQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopOrderNoticeUpsertRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopOrderNoticeResponse;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeOwnerCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeUpsertCommand;

@Tag(name = "Ceo Shop Order Notice", description = "점주 주문안내 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOrderNoticeApiController {
    private final ShopOrderNoticeOwnerQueryUseCase shopOrderNoticeQueryService;
    private final ShopOrderNoticeOwnerCommandUseCase shopOrderNoticeCommandUseCase;

    public ShopOrderNoticeApiController(
        ShopOrderNoticeOwnerQueryUseCase shopOrderNoticeQueryService,
        ShopOrderNoticeOwnerCommandUseCase shopOrderNoticeCommandUseCase
    ) {
        this.shopOrderNoticeQueryService = shopOrderNoticeQueryService;
        this.shopOrderNoticeCommandUseCase = shopOrderNoticeCommandUseCase;
    }

    @Operation(summary = "주문안내 조회", description = "가게의 주문안내를 조회합니다. 미설정이면 content가 null이며, 관리자 게시중단 여부와 사유가 함께 내려갑니다.")
    @GetMapping("/v1/{id}/order-notice")
    public ResponseEntity<ApiResponse<ShopOrderNoticeResponse>> getOrderNotice(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopOrderNoticeResponse response = shopOrderNoticeQueryService.getOrderNotice(userDetails.getCeoId(), id)
            .map(ShopOrderNoticeResponse::from)
            .orElseGet(ShopOrderNoticeResponse::empty);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문안내 등록·수정", description = "가게의 주문안내를 등록하거나 수정합니다(가게당 1건 전체교체). 승인 절차 없이 즉시 손님 화면에 반영됩니다.")
    @PutMapping("/v1/{id}/order-notice")
    public ResponseEntity<ApiResponse<Void>> upsertOrderNotice(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopOrderNoticeUpsertRequest request
    ) {
        ShopOrderNoticeUpsertCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopOrderNoticeCommandUseCase.upsertOrderNotice(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
