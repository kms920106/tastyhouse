package com.tastyhouse.adminapi.shop.adapter.in.web;

import com.tastyhouse.application.shop.port.in.ShopOrderNoticeManagementCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeHideCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeUnhideCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopOrderNoticeHideRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopOrderNoticeResponse;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeManagementQueryUseCase;

@Tag(name = "Shop Order Notice Admin", description = "주문안내 검수 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOrderNoticeAdminApiController {
    private final ShopOrderNoticeManagementCommandUseCase shopOrderNoticeCommandUseCase;
    private final ShopOrderNoticeManagementQueryUseCase shopOrderNoticeQueryUseCase;

    public ShopOrderNoticeAdminApiController(
        ShopOrderNoticeManagementCommandUseCase shopOrderNoticeCommandUseCase,
        ShopOrderNoticeManagementQueryUseCase shopOrderNoticeQueryUseCase
    ) {
        this.shopOrderNoticeCommandUseCase = shopOrderNoticeCommandUseCase;
        this.shopOrderNoticeQueryUseCase = shopOrderNoticeQueryUseCase;
    }

    @Operation(summary = "주문안내 조회", description = "가게의 주문안내 본문과 게시중단 여부·사유를 조회합니다.")
    @GetMapping("/v1/{id}/order-notice")
    public ResponseEntity<ApiResponse<ShopOrderNoticeResponse>> getOrderNotice(@PathVariable Long id) {
        ShopOrderNoticeResponse response = shopOrderNoticeQueryUseCase.getOrderNotice(id)
            .map(ShopOrderNoticeResponse::from)
            .orElseGet(ShopOrderNoticeResponse::empty);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문안내 게시중단", description = "규정을 위반한 주문안내를 게시중단합니다. 사유는 점주 조회 응답으로 전달됩니다.")
    @PatchMapping("/v1/{id}/order-notice/hide")
    public ResponseEntity<ApiResponse<Void>> hideOrderNotice(
        @PathVariable Long id,
        @Valid @RequestBody ShopOrderNoticeHideRequest request
    ) {
        ShopOrderNoticeHideCommand command = request.toCommand(id);
        shopOrderNoticeCommandUseCase.hideOrderNotice(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "주문안내 게시중단 해제", description = "게시중단된 주문안내를 다시 게시합니다. 점주가 마지막으로 저장한 문구가 그대로 복원됩니다.")
    @PatchMapping("/v1/{id}/order-notice/unhide")
    public ResponseEntity<ApiResponse<Void>> unhideOrderNotice(@PathVariable Long id) {
        ShopOrderNoticeUnhideCommand command = ShopOrderNoticeUnhideCommand.of(id);
        shopOrderNoticeCommandUseCase.unhideOrderNotice(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
