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

import com.tastyhouse.application.shop.port.in.ShopOriginInfoOwnerQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopOriginInfoUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopOriginInfoResponse;
import com.tastyhouse.application.shop.port.in.ShopOriginInfoCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopOriginInfoUpdateCommand;

@Tag(name = "Ceo Shop Origin Info", description = "점주 가게 원산지 표시 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopOriginInfoApiController {
    private final ShopOriginInfoOwnerQueryUseCase shopOriginInfoQueryService;
    private final ShopOriginInfoCommandUseCase shopOriginInfoCommandUseCase;

    public ShopOriginInfoApiController(ShopOriginInfoOwnerQueryUseCase shopOriginInfoQueryService, ShopOriginInfoCommandUseCase shopOriginInfoCommandUseCase) {
        this.shopOriginInfoQueryService = shopOriginInfoQueryService;
        this.shopOriginInfoCommandUseCase = shopOriginInfoCommandUseCase;
    }

    @Operation(summary = "내 가게 원산지 조회",
        description = "로그인한 점주가 소유한 가게의 원산지 표시 정보를 조회합니다. 미설정이어도 data는 "
            + "null이 아니라 sourceType=DIRECT·content=null로 내려가므로 화면이 분기 없이 빈 폼을 그릴 수 있습니다.")
    @GetMapping("/v1/{id}/origin")
    public ResponseEntity<ApiResponse<ShopOriginInfoResponse>> getOriginInfo(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopOriginInfoResponse response =
            shopOriginInfoQueryService.getOriginInfo(userDetails.getCeoId(), id)
                .map(ShopOriginInfoResponse::from)
                .orElseGet(ShopOriginInfoResponse::empty);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 원산지 등록/수정",
        description = "원산지 표시 정보를 전체 교체합니다. sourceType=DIRECT면 content가, FRANCHISE_URL이면 "
            + "url이 필수이며, 입력 방식이 바뀌면 서버가 반대편 필드를 null로 정리합니다.")
    @PutMapping("/v1/{id}/origin")
    public ResponseEntity<ApiResponse<Void>> updateOriginInfo(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopOriginInfoUpdateRequest request
    ) {
        ShopOriginInfoUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopOriginInfoCommandUseCase.updateOriginInfo(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
