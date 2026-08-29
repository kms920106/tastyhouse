package com.tastyhouse.ceoapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.ceoapi.shop.application.service.ShopRiderGuideQueryService;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopRiderPickupLocationUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopRiderVisitGuideUpdateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopRiderVisitGuideValidateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRiderGuideResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRiderVisitGuideValidationResponse;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopRiderGuideCommandUseCase;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopRiderPickupLocationClearCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopRiderPickupLocationUpdateCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopRiderVisitGuideUpdateCommand;

@Tag(name = "Ceo Shop Rider Guide", description = "점주 라이더 가게방문 안내·픽업 위치 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopRiderGuideApiController {

    private final ShopRiderGuideQueryService shopRiderGuideQueryService;
    private final ShopRiderGuideCommandUseCase shopRiderGuideCommandUseCase;

    public ShopRiderGuideApiController(ShopRiderGuideQueryService shopRiderGuideQueryService, ShopRiderGuideCommandUseCase shopRiderGuideCommandUseCase) {
        this.shopRiderGuideQueryService = shopRiderGuideQueryService;
        this.shopRiderGuideCommandUseCase = shopRiderGuideCommandUseCase;
    }

    @Operation(summary = "내 가게 라이더 안내 조회",
        description = "로그인한 점주가 소유한 가게의 라이더 가게방문 안내 문구와 픽업 위치를 함께 조회합니다. 미등록 가게도 정상 응답(null)을 반환합니다.")
    @GetMapping("/v1/{id}/rider-guide")
    public ResponseEntity<ApiResponse<ShopRiderGuideResponse>> getRiderGuide(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopRiderGuideResponse response = shopRiderGuideQueryService.getRiderGuide(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 라이더 안내 문구 등록",
        description = "라이더 가게방문 안내 문구를 등록·수정합니다(최대 200자). 빈 문자열을 보내면 등록된 문구가 삭제됩니다.")
    @PutMapping("/v1/{id}/rider-guide/visit-guide")
    public ResponseEntity<ApiResponse<Void>> updateVisitGuide(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopRiderVisitGuideUpdateRequest request
    ) {
        ShopRiderVisitGuideUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopRiderGuideCommandUseCase.updateVisitGuide(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "라이더 안내 문구 사전 검수",
        description = "등록 전 라이더 가게방문 안내 문구가 등록 기준(금칙어·가게 실주소 재기재·배차 특정)을 위반하는지 미리 검수합니다. 위반이 있어도 200으로 사유 목록을 반환합니다.")
    @PostMapping("/v1/{id}/rider-guide/visit-guide/validate")
    public ResponseEntity<ApiResponse<ShopRiderVisitGuideValidationResponse>> validateVisitGuide(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopRiderVisitGuideValidateRequest request
    ) {
        ShopRiderVisitGuideValidationResponse response = shopRiderGuideQueryService.validateVisitGuide(
            userDetails.getCeoId(), id, request.visitGuide()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 라이더 픽업 위치 등록",
        description = "가게 실주소와 별도로 관리되는 라이더 픽업 위치를 등록·수정합니다. 가게 실주소·좌표는 변경되지 않으므로 배달가능지역·배달팁에 영향이 없습니다.")
    @PutMapping("/v1/{id}/rider-guide/pickup-location")
    public ResponseEntity<ApiResponse<Void>> updatePickupLocation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopRiderPickupLocationUpdateRequest request
    ) {
        ShopRiderPickupLocationUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopRiderGuideCommandUseCase.updatePickupLocation(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내 가게 라이더 픽업 위치 초기화",
        description = "라이더 픽업 위치를 비워 가게 실주소로 폴백시킵니다. 안내 문구는 유지되며, 이미 미설정 상태에서 호출해도 정상 처리됩니다.")
    @DeleteMapping("/v1/{id}/rider-guide/pickup-location")
    public ResponseEntity<ApiResponse<Void>> clearPickupLocation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopRiderPickupLocationClearCommand command = ShopRiderPickupLocationClearCommand.of(userDetails.getCeoId(), id);
        shopRiderGuideCommandUseCase.clearPickupLocation(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
