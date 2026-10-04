package com.tastyhouse.ceoapi.shop.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaRadiusApplyCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaRadiusQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryAreaRadiusRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaBulkResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaRadiusPreviewResponse;

@Tag(name = "Ceo Shop Delivery Area Radius", description = "점주 가게 반경 배달가능지역 API")
@RestController
@RequestMapping("/api/shops")
public class ShopDeliveryAreaRadiusApiController {

    private final ShopDeliveryAreaRadiusQueryUseCase shopDeliveryAreaRadiusQueryUseCase;
    private final ShopDeliveryAreaCommandUseCase shopDeliveryAreaCommandUseCase;

    public ShopDeliveryAreaRadiusApiController(
        ShopDeliveryAreaRadiusQueryUseCase shopDeliveryAreaRadiusQueryUseCase,
        ShopDeliveryAreaCommandUseCase shopDeliveryAreaCommandUseCase
    ) {
        this.shopDeliveryAreaRadiusQueryUseCase = shopDeliveryAreaRadiusQueryUseCase;
        this.shopDeliveryAreaCommandUseCase = shopDeliveryAreaCommandUseCase;
    }

    @Operation(
        summary = "반경 배달가능지역 미리보기",
        description = "가게 주소를 기준으로 지정한 반경 안에 드는 행정동을 미리 조회합니다. 저장하지 않습니다."
    )
    @GetMapping("/v1/{id}/delivery-areas/radius-preview")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaRadiusPreviewResponse>> getRadiusPreview(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @RequestParam @Min(value = 500, message = "반경은 500m 이상이어야 합니다.")
        @Max(value = 7000, message = "반경은 7000m를 넘을 수 없습니다.") int radiusMeters
    ) {
        ShopDeliveryAreaRadiusPreviewResponse response =
            ShopDeliveryAreaRadiusPreviewResponse.from(shopDeliveryAreaRadiusQueryUseCase.previewRadius(userDetails.getCeoId(), id, radiusMeters));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "반경 배달가능지역 적용",
        description = "지정한 반경 안에 드는 행정동을 배달가능지역으로 등록합니다. replace가 true면 반경 밖의 기존 직접 등록분을 정리합니다."
    )
    @PostMapping("/v1/{id}/delivery-areas/radius")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaBulkResponse>> applyRadius(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaRadiusRequest request
    ) {
        ShopDeliveryAreaRadiusApplyCommand command = request.toCommand(userDetails.getCeoId(), id);
        ShopDeliveryAreaBulkResponse response =
            ShopDeliveryAreaBulkResponse.from(shopDeliveryAreaCommandUseCase.applyRadius(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
