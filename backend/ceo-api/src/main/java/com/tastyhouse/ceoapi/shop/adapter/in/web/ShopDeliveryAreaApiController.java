package com.tastyhouse.ceoapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaPolygonQueryUseCase;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaQueryUseCase;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaRadiusQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryAreaBulkRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryAreaCreateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryAreaPolygonSaveRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryAreaRadiusRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaBulkDeleteResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaBulkResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaItemResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaPolygonPreviewResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaPolygonResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaRadiusPreviewResponse;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaBulkCreateCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaBulkDeleteCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaCommandUseCase;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaCreateCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaDeleteCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaPolygonDeleteCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaPolygonSaveCommand;
import com.tastyhouse.ceoapi.shop.application.port.in.ShopDeliveryAreaRadiusApplyCommand;

@Tag(name = "Ceo Shop Delivery Area", description = "점주 가게 배달가능지역 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopDeliveryAreaApiController {

    private final ShopDeliveryAreaQueryUseCase shopDeliveryAreaQueryService;
    private final ShopDeliveryAreaCommandUseCase shopDeliveryAreaCommandUseCase;
    private final ShopDeliveryAreaRadiusQueryUseCase shopDeliveryAreaRadiusQueryService;
    private final ShopDeliveryAreaPolygonQueryUseCase shopDeliveryAreaPolygonQueryService;

    public ShopDeliveryAreaApiController(
        ShopDeliveryAreaQueryUseCase shopDeliveryAreaQueryService,
        ShopDeliveryAreaCommandUseCase shopDeliveryAreaCommandUseCase,
        ShopDeliveryAreaRadiusQueryUseCase shopDeliveryAreaRadiusQueryService,
        ShopDeliveryAreaPolygonQueryUseCase shopDeliveryAreaPolygonQueryService
    ) {
        this.shopDeliveryAreaQueryService = shopDeliveryAreaQueryService;
        this.shopDeliveryAreaCommandUseCase = shopDeliveryAreaCommandUseCase;
        this.shopDeliveryAreaRadiusQueryService = shopDeliveryAreaRadiusQueryService;
        this.shopDeliveryAreaPolygonQueryService = shopDeliveryAreaPolygonQueryService;
    }

    @Operation(summary = "내 가게 배달가능지역 조회", description = "로그인한 점주가 소유한 가게의 배달가능지역(행정동) 목록을 조회합니다.")
    @GetMapping("/v1/{id}/delivery-areas")
    public ResponseEntity<ApiResponse<List<ShopDeliveryAreaItemResponse>>> getDeliveryAreas(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopDeliveryAreaItemResponse> response = shopDeliveryAreaQueryService.getDeliveryAreas(userDetails.getCeoId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 배달가능지역 추가", description = "로그인한 점주가 소유한 가게에 배달가능지역(행정동)을 추가합니다.")
    @PostMapping("/v1/{id}/delivery-areas")
    public ResponseEntity<ApiResponse<Long>> createDeliveryArea(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaCreateRequest request
    ) {
        ShopDeliveryAreaCreateCommand command = request.toCommand(userDetails.getCeoId(), id);
        Long deliveryAreaId = shopDeliveryAreaCommandUseCase.addDeliveryArea(command);
        return ResponseEntity.ok(ApiResponse.success(deliveryAreaId));
    }

    @Operation(summary = "내 가게 배달가능지역 삭제", description = "로그인한 점주가 소유한 가게의 배달가능지역을 삭제합니다. 해당 지역에 지역별 배달팁이 설정돼 있으면 삭제할 수 없습니다.")
    @DeleteMapping("/v1/delivery-areas/{deliveryAreaId}")
    public ResponseEntity<ApiResponse<Void>> deleteDeliveryArea(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long deliveryAreaId
    ) {
        ShopDeliveryAreaDeleteCommand command = ShopDeliveryAreaDeleteCommand.of(userDetails.getCeoId(), deliveryAreaId);
        shopDeliveryAreaCommandUseCase.removeDeliveryArea(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "내 가게 배달가능지역 일괄 추가",
        description = "행정동을 한 번에 여러 개 추가합니다. 이미 등록된 행정동은 오류가 아니라 건너뛰며, 존재하지 않는 행정동이 섞이면 전체가 실패합니다."
    )
    @PostMapping("/v1/{id}/delivery-areas/bulk")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaBulkResponse>> createDeliveryAreasBulk(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaBulkRequest request
    ) {
        ShopDeliveryAreaBulkCreateCommand command = request.toCreateCommand(userDetails.getCeoId(), id);
        ShopDeliveryAreaBulkResponse response = shopDeliveryAreaCommandUseCase.addDeliveryAreas(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "내 가게 배달가능지역 일괄 삭제",
        description = "행정동을 한 번에 여러 개 삭제합니다. 지역별 배달팁이 설정된 행정동이 하나라도 포함되면 한 건도 삭제하지 않고 실패합니다."
    )
    @PostMapping("/v1/{id}/delivery-areas/bulk-delete")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaBulkDeleteResponse>> deleteDeliveryAreasBulk(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaBulkRequest request
    ) {
        ShopDeliveryAreaBulkDeleteCommand command = request.toDeleteCommand(userDetails.getCeoId(), id);
        ShopDeliveryAreaBulkDeleteResponse response = shopDeliveryAreaCommandUseCase.removeDeliveryAreas(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "반경 배달가능지역 미리보기",
        description = "가게 주소를 기준으로 지정한 반경 안에 드는 행정동을 미리 조회합니다. 저장하지 않습니다."
    )
    @GetMapping("/v1/{id}/delivery-areas/radius-preview")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaRadiusPreviewResponse>> getRadiusPreview(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @RequestParam @Min(value = 500, message = "반경은 500m 이상이어야 합니다.")
        @Max(value = 7000, message = "반경은 7000m를 넘을 수 없습니다.") int radiusMeters
    ) {
        ShopDeliveryAreaRadiusPreviewResponse response = shopDeliveryAreaRadiusQueryService.previewRadius(
            userDetails.getCeoId(), id, radiusMeters
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "반경 배달가능지역 적용",
        description = "지정한 반경 안에 드는 행정동을 배달가능지역으로 등록합니다. replace가 true면 반경 밖의 기존 직접 등록분을 정리합니다."
    )
    @PostMapping("/v1/{id}/delivery-areas/radius")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaBulkResponse>> applyRadius(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaRadiusRequest request
    ) {
        ShopDeliveryAreaRadiusApplyCommand command = request.toCommand(userDetails.getCeoId(), id);
        ShopDeliveryAreaBulkResponse response = shopDeliveryAreaCommandUseCase.applyRadius(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "배달지역 도형 조회",
        description = "저장된 배달지역 도형을 조회합니다. 도형을 설정하지 않은 상태는 오류가 아니라 exists=false로 응답합니다."
    )
    @GetMapping("/v1/{id}/delivery-areas/polygon")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaPolygonResponse>> getPolygon(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopDeliveryAreaPolygonResponse response = shopDeliveryAreaPolygonQueryService.getPolygon(
            userDetails.getCeoId(), id
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "배달지역 도형 저장",
        description = "배달지역 도형을 저장하고 행정동으로 환산합니다(전체 교체). 환산 결과가 0건이거나 7km를 넘으면 실패합니다."
    )
    @PutMapping("/v1/{id}/delivery-areas/polygon")
    public ResponseEntity<ApiResponse<Void>> savePolygon(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaPolygonSaveRequest request
    ) {
        ShopDeliveryAreaPolygonSaveCommand command = request.toCommand(userDetails.getCeoId(), id);
        shopDeliveryAreaCommandUseCase.savePolygon(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * HTTP 메서드는 {@code POST}지만 <b>의미는 조회</b>다 — 도형이 URL에 들어갈 수 없어 본문으로 받을
     * 뿐이며, 저장하지 않고 환산 결과만 계산해 돌려준다.
     */
    @Operation(
        summary = "배달지역 도형 환산 미리보기",
        description = "도형을 저장하지 않고 환산 결과만 조회합니다. 저장 시 열리는 행정동·닫히는 행정동·배달팁 때문에 닫을 수 없는 행정동을 함께 알려줍니다."
    )
    @PostMapping("/v1/{id}/delivery-areas/polygon/preview")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaPolygonPreviewResponse>> previewPolygon(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaPolygonSaveRequest request
    ) {
        ShopDeliveryAreaPolygonPreviewResponse response = shopDeliveryAreaPolygonQueryService.previewPolygon(
            userDetails.getCeoId(), id, request.rings()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "배달지역 도형 삭제",
        description = "도형과 그로부터 파생된 배달가능지역을 삭제합니다. 직접 등록한 행정동은 남습니다."
    )
    @DeleteMapping("/v1/{id}/delivery-areas/polygon")
    public ResponseEntity<ApiResponse<Void>> deletePolygon(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id
    ) {
        ShopDeliveryAreaPolygonDeleteCommand command = ShopDeliveryAreaPolygonDeleteCommand.of(userDetails.getCeoId(), id);
        shopDeliveryAreaCommandUseCase.deletePolygon(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
