package com.tastyhouse.ceoapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.ceoapplication.shop.port.in.ShopIntroductionQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapplication.auth.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopIntroductionUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopIntroductionValidateRequest;
import com.tastyhouse.ceoapplication.shop.response.ShopIntroductionResponse;
import com.tastyhouse.ceoapplication.shop.response.ShopIntroductionValidationResponse;
import com.tastyhouse.ceoapplication.shop.port.in.ShopIntroductionCommandUseCase;
import com.tastyhouse.ceoapplication.shop.port.in.ShopIntroductionUpdateCommand;

@Tag(name = "Ceo Shop Introduction", description = "점주 가게소개(사장님 한마디) 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopIntroductionApiController {

    private final ShopIntroductionQueryUseCase shopIntroductionQueryService;
    private final ShopIntroductionCommandUseCase shopIntroductionCommandUseCase;

    public ShopIntroductionApiController(ShopIntroductionQueryUseCase shopIntroductionQueryService, ShopIntroductionCommandUseCase shopIntroductionCommandUseCase) {
        this.shopIntroductionQueryService = shopIntroductionQueryService;
        this.shopIntroductionCommandUseCase = shopIntroductionCommandUseCase;
    }

    @Operation(summary = "내 가게소개 조회", description = "로그인한 점주가 소유한 가게의 최근 가게소개(사장님 한마디)를 조회합니다.")
    @GetMapping("/v1/{id}/introduction")
    public ResponseEntity<ApiResponse<ShopIntroductionResponse>> getIntroduction(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopIntroductionResponse response = shopIntroductionQueryService.getIntroduction(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게소개 등록", description = "로그인한 점주가 소유한 가게의 가게소개(사장님 한마디)를 새로 등록합니다(최대 500자, 금칙어 검수 포함).")
    @PutMapping("/v1/{id}/introduction")
    public ResponseEntity<ApiResponse<Void>> updateIntroduction(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopIntroductionUpdateRequest request
    ) {
        ShopIntroductionUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopIntroductionCommandUseCase.updateIntroduction(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가게소개 금칙어 검수", description = "등록 전 가게소개 메시지에 금칙어가 포함되어 있는지 미리 검수합니다.")
    @PostMapping("/v1/{id}/introduction/validate")
    public ResponseEntity<ApiResponse<ShopIntroductionValidationResponse>> validateIntroduction(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopIntroductionValidateRequest request
    ) {
        ShopIntroductionValidationResponse response = shopIntroductionQueryService.validateIntroduction(
            userDetails.getCeoId(), id, request.message()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
