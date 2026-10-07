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

import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonDeleteUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonDetailQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonPreviewQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonSaveCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonSaveUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryAreaPolygonSaveRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaPolygonPreviewResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaPolygonResponse;

@Tag(name = "Ceo Shop Delivery Area Polygon", description = "점주 가게 배달지역 도형 API")
@RestController
@RequestMapping("/api/shops")
class ShopDeliveryAreaPolygonApiController {

    private final ShopDeliveryAreaPolygonDetailQueryUseCase shopDeliveryAreaPolygonDetailQueryUseCase;
    private final ShopDeliveryAreaPolygonSaveUseCase shopDeliveryAreaPolygonSaveUseCase;
    private final ShopDeliveryAreaPolygonPreviewQueryUseCase shopDeliveryAreaPolygonPreviewQueryUseCase;
    private final ShopDeliveryAreaPolygonDeleteUseCase shopDeliveryAreaPolygonDeleteUseCase;

    public ShopDeliveryAreaPolygonApiController(
        ShopDeliveryAreaPolygonDetailQueryUseCase shopDeliveryAreaPolygonDetailQueryUseCase,
        ShopDeliveryAreaPolygonSaveUseCase shopDeliveryAreaPolygonSaveUseCase,
        ShopDeliveryAreaPolygonPreviewQueryUseCase shopDeliveryAreaPolygonPreviewQueryUseCase,
        ShopDeliveryAreaPolygonDeleteUseCase shopDeliveryAreaPolygonDeleteUseCase
    ) {
        this.shopDeliveryAreaPolygonDetailQueryUseCase = shopDeliveryAreaPolygonDetailQueryUseCase;
        this.shopDeliveryAreaPolygonSaveUseCase = shopDeliveryAreaPolygonSaveUseCase;
        this.shopDeliveryAreaPolygonPreviewQueryUseCase = shopDeliveryAreaPolygonPreviewQueryUseCase;
        this.shopDeliveryAreaPolygonDeleteUseCase = shopDeliveryAreaPolygonDeleteUseCase;
    }

    @Operation(
        summary = "배달지역 도형 조회",
        description = "저장된 배달지역 도형을 조회합니다. 도형을 설정하지 않은 상태는 오류가 아니라 exists=false로 응답합니다."
    )
    @GetMapping("/v1/{id}/delivery-areas/polygon")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaPolygonResponse>> getPolygon(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopDeliveryAreaPolygonResponse response =
            ShopDeliveryAreaPolygonResponse.from(shopDeliveryAreaPolygonDetailQueryUseCase.getPolygon(userDetails.getCeoId(), id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "배달지역 도형 저장",
        description = "배달지역 도형을 저장하고 행정동으로 환산합니다(전체 교체). 환산 결과가 0건이거나 7km를 넘으면 실패합니다."
    )
    @PutMapping("/v1/{id}/delivery-areas/polygon")
    public ResponseEntity<ApiResponse<Void>> savePolygon(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaPolygonSaveRequest request
    ) {
        ShopDeliveryAreaPolygonSaveCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopDeliveryAreaPolygonSaveUseCase.savePolygon(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "배달지역 도형 환산 미리보기",
        description = "도형을 저장하지 않고 환산 결과만 조회합니다. 저장 시 열리는 행정동·닫히는 행정동·배달팁 때문에 닫을 수 없는 행정동을 함께 알려줍니다."
    )
    @PostMapping("/v1/{id}/delivery-areas/polygon/preview")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaPolygonPreviewResponse>> previewPolygon(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaPolygonSaveRequest request
    ) {
        ShopDeliveryAreaPolygonPreviewResponse response =
            ShopDeliveryAreaPolygonPreviewResponse.from(shopDeliveryAreaPolygonPreviewQueryUseCase.previewPolygon(userDetails.getCeoId(), id, request.toRingCommands()));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "배달지역 도형 삭제",
        description = "도형과 그로부터 파생된 배달가능지역을 삭제합니다. 직접 등록한 행정동은 남습니다."
    )
    @DeleteMapping("/v1/{id}/delivery-areas/polygon")
    public ResponseEntity<ApiResponse<Void>> deletePolygon(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopDeliveryAreaPolygonDeleteCommand command = ShopDeliveryAreaPolygonDeleteCommand.of(userDetails.getCeoId(), id);
        shopDeliveryAreaPolygonDeleteUseCase.deletePolygon(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
