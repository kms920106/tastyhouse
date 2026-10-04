package com.tastyhouse.ceoapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.CeoUserDetails;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaBulkDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaDeleteCommand;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryAreaBulkRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.request.ShopDeliveryAreaCreateRequest;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaBulkDeleteResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaBulkResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopDeliveryAreaItemResponse;

@Tag(name = "Ceo Shop Delivery Area", description = "점주 가게 배달가능지역 관리 API")
@RestController
@RequestMapping("/api/shops")
public class ShopDeliveryAreaApiController {

    private final ShopDeliveryAreaQueryUseCase shopDeliveryAreaQueryUseCase;
    private final ShopDeliveryAreaCommandUseCase shopDeliveryAreaCommandUseCase;

    public ShopDeliveryAreaApiController(
        ShopDeliveryAreaQueryUseCase shopDeliveryAreaQueryUseCase,
        ShopDeliveryAreaCommandUseCase shopDeliveryAreaCommandUseCase
    ) {
        this.shopDeliveryAreaQueryUseCase = shopDeliveryAreaQueryUseCase;
        this.shopDeliveryAreaCommandUseCase = shopDeliveryAreaCommandUseCase;
    }

    @Operation(summary = "내 가게 배달가능지역 조회", description = "로그인한 점주가 소유한 가게의 배달가능지역(행정동) 목록을 조회합니다.")
    @GetMapping("/v1/{id}/delivery-areas")
    public ResponseEntity<ApiResponse<List<ShopDeliveryAreaItemResponse>>> getDeliveryAreas(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        List<ShopDeliveryAreaItemResponse> response = shopDeliveryAreaQueryUseCase.getDeliveryAreas(userDetails.getCeoId(), id).stream()
            .map(ShopDeliveryAreaItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 가게 배달가능지역 추가", description = "로그인한 점주가 소유한 가게에 배달가능지역(행정동)을 추가합니다.")
    @PostMapping("/v1/{id}/delivery-areas")
    public ResponseEntity<ApiResponse<Long>> createDeliveryArea(
        @AuthenticationPrincipal CeoUserDetails userDetails,
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
        @AuthenticationPrincipal CeoUserDetails userDetails,
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
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaBulkRequest request
    ) {
        ShopDeliveryAreaBulkCreateCommand command = request.toCreateCommand(userDetails.getCeoId(), id);
        ShopDeliveryAreaBulkResponse response =
            ShopDeliveryAreaBulkResponse.from(shopDeliveryAreaCommandUseCase.addDeliveryAreas(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "내 가게 배달가능지역 일괄 삭제",
        description = "행정동을 한 번에 여러 개 삭제합니다. 지역별 배달팁이 설정된 행정동이 하나라도 포함되면 한 건도 삭제하지 않고 실패합니다."
    )
    @PostMapping("/v1/{id}/delivery-areas/bulk-delete")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaBulkDeleteResponse>> deleteDeliveryAreasBulk(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody ShopDeliveryAreaBulkRequest request
    ) {
        ShopDeliveryAreaBulkDeleteCommand command = request.toDeleteCommand(userDetails.getCeoId(), id);
        ShopDeliveryAreaBulkDeleteResponse response =
            ShopDeliveryAreaBulkDeleteResponse.from(shopDeliveryAreaCommandUseCase.removeDeliveryAreas(command));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
