package com.tastyhouse.ceoapi.shop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.ceoapi.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.request.ShopStatusUpdateRequest;
import com.tastyhouse.ceoapi.shop.response.ShopStatusResponse;

@Tag(name = "Ceo Shop Status", description = "점주 가게 노출 상태 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shops")
public class ShopStatusApiController {

    private final ShopStatusQueryService shopStatusQueryService;
    private final ShopStatusCommandService shopStatusCommandService;

    @Operation(summary = "내 가게 노출 상태 조회", description = "로그인한 점주가 소유한 가게의 노출정지·폐업 상태를 조회합니다.")
    @GetMapping("/v1/{id}/status")
    public ResponseEntity<ApiResponse<ShopStatusResponse>> getStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopStatusResponse response = shopStatusQueryService.getStatus(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 노출 상태 변경", description = "로그인한 점주가 소유한 가게를 노출정지하거나 다시 노출합니다. 진행 중인 승인 요청이 있으면 변경이 차단됩니다.")
    @PutMapping("/v1/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopStatusUpdateRequest request
    ) {
        shopStatusCommandService.updateStatus(userDetails.getCeoId(), id, request.status());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
