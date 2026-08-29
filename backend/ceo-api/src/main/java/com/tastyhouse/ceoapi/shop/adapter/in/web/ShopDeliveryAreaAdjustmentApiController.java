package com.tastyhouse.ceoapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.ceoapi.shop.application.service.ShopDeliveryAreaAdjustmentQueryService;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryAreaAdjustmentCreateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaAdjustmentItemResponse;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaAdjustmentCommandUseCase;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaAdjustmentCreateCommand;

@Tag(name = "Ceo Shop Delivery Area Adjustment", description = "점주 프랜차이즈 배달지역 조정 신청 API")
@RestController
@RequestMapping("/api/shops")
public class ShopDeliveryAreaAdjustmentApiController {

    private final ShopDeliveryAreaAdjustmentQueryService shopDeliveryAreaAdjustmentQueryService;
    private final ShopDeliveryAreaAdjustmentCommandUseCase shopDeliveryAreaAdjustmentCommandUseCase;

    public ShopDeliveryAreaAdjustmentApiController(
        ShopDeliveryAreaAdjustmentQueryService shopDeliveryAreaAdjustmentQueryService,
        ShopDeliveryAreaAdjustmentCommandUseCase shopDeliveryAreaAdjustmentCommandUseCase
    ) {
        this.shopDeliveryAreaAdjustmentQueryService = shopDeliveryAreaAdjustmentQueryService;
        this.shopDeliveryAreaAdjustmentCommandUseCase = shopDeliveryAreaAdjustmentCommandUseCase;
    }

    @Operation(summary = "내 가게 배달지역 조정 신청 이력 조회", description = "로그인한 점주가 소유한 가게의 배달지역 조정 신청 이력을 최근순으로 조회합니다.")
    @GetMapping("/v1/{id}/delivery-area-adjustments")
    public ResponseEntity<ApiResponse<List<ShopDeliveryAreaAdjustmentItemResponse>>> getAdjustmentRequests(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopDeliveryAreaAdjustmentItemResponse> response =
            shopDeliveryAreaAdjustmentQueryService.getAdjustmentRequests(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "배달지역 조정 신청",
        description = "가맹점 간 배달지역 중첩에 대한 가맹본부 중재를 신청합니다. 정보제공 동의서 파일을 함께 첨부합니다. (jpg/png/gif/webp/pdf, 최대 10MB)"
    )
    @PostMapping(value = "/v1/{id}/delivery-area-adjustments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> requestAdjustment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @ModelAttribute ShopDeliveryAreaAdjustmentCreateRequest request,
        @Parameter(description = "조정신청 관련 정보제공 동의서 파일", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        ShopDeliveryAreaAdjustmentCreateCommand command = request.toCommand(userDetails.getCeoId(), id);
        Long requestId = shopDeliveryAreaAdjustmentCommandUseCase.requestAdjustment(command, file);
        return ResponseEntity.ok(ApiResponse.success(requestId));
    }
}
