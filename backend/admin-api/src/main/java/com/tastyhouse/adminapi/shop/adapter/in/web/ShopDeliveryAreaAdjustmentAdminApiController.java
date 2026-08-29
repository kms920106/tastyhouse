package com.tastyhouse.adminapi.shop.adapter.in.web;

import com.tastyhouse.adminapi.shop.application.port.in.ShopDeliveryAreaAdjustmentCommandUseCase;
import com.tastyhouse.adminapi.shop.application.port.in.ShopDeliveryAreaAdjustmentRejectCommand;
import com.tastyhouse.adminapi.shop.application.port.in.ShopDeliveryAreaAdjustmentStatusChangeCommand;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopDeliveryAreaAdjustmentRejectRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopDeliveryAreaAdjustmentSearchRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopDeliveryAreaAdjustmentStatusChangeRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopDeliveryAreaAdjustmentDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopDeliveryAreaAdjustmentListItemResponse;
import com.tastyhouse.adminapi.shop.application.port.in.ShopDeliveryAreaAdjustmentQueryUseCase;

@Tag(name = "Shop Delivery Area Adjustment Admin", description = "프랜차이즈 배달지역 조정 신청 검수 관리자 API")
@RestController
@RequestMapping("/api/shops")
public class ShopDeliveryAreaAdjustmentAdminApiController {

    private final ShopDeliveryAreaAdjustmentQueryUseCase shopDeliveryAreaAdjustmentQueryUseCase;
    private final ShopDeliveryAreaAdjustmentCommandUseCase shopDeliveryAreaAdjustmentCommandUseCase;

    public ShopDeliveryAreaAdjustmentAdminApiController(
        ShopDeliveryAreaAdjustmentQueryUseCase shopDeliveryAreaAdjustmentQueryUseCase,
        ShopDeliveryAreaAdjustmentCommandUseCase shopDeliveryAreaAdjustmentCommandUseCase
    ) {
        this.shopDeliveryAreaAdjustmentQueryUseCase = shopDeliveryAreaAdjustmentQueryUseCase;
        this.shopDeliveryAreaAdjustmentCommandUseCase = shopDeliveryAreaAdjustmentCommandUseCase;
    }

    @Operation(summary = "배달지역 조정 신청 목록 조회", description = "프랜차이즈 배달지역 조정 신청 목록을 상태·가게로 필터해 페이징 조회합니다.")
    @GetMapping("/v1/delivery-area-adjustments")
    public ResponseEntity<ApiResponse<List<ShopDeliveryAreaAdjustmentListItemResponse>>> getAdjustmentRequests(
        @Valid @ModelAttribute ShopDeliveryAreaAdjustmentSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ShopDeliveryAreaAdjustmentListItemResponse> pageResponse = shopDeliveryAreaAdjustmentQueryUseCase.getAdjustmentRequests(
            search.status(), search.shopId(), pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(summary = "배달지역 조정 신청 상세 조회", description = "조정 신청의 중첩 사유와 정보제공 동의서를 포함한 상세를 조회합니다.")
    @GetMapping("/v1/delivery-area-adjustments/{requestId}")
    public ResponseEntity<ApiResponse<ShopDeliveryAreaAdjustmentDetailResponse>> getAdjustmentRequest(@PathVariable Long requestId) {
        ShopDeliveryAreaAdjustmentDetailResponse response = shopDeliveryAreaAdjustmentQueryUseCase.getAdjustmentRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "배달지역 조정 신청 상태 변경",
        description = "접수 대기 → 조정 중(가맹본부 전달), 조정 중 → 조정 완료로 전이합니다. 조정 완료는 성립 사실의 기록일 뿐 배달가능지역을 자동 반영하지 않습니다."
    )
    @PatchMapping("/v1/delivery-area-adjustments/{requestId}/status")
    public ResponseEntity<ApiResponse<Void>> changeAdjustmentStatus(
        @PathVariable Long requestId,
        @Valid @RequestBody ShopDeliveryAreaAdjustmentStatusChangeRequest request
    ) {
        ShopDeliveryAreaAdjustmentStatusChangeCommand command = request.toCommand(requestId);
        shopDeliveryAreaAdjustmentCommandUseCase.changeStatus(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "배달지역 조정 신청 반려", description = "형식 미비·조정 불성립 등의 사유로 신청을 반려합니다. 접수 대기·조정 중 어느 쪽에서든 반려할 수 있습니다.")
    @PatchMapping("/v1/delivery-area-adjustments/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectAdjustment(
        @PathVariable Long requestId,
        @Valid @RequestBody ShopDeliveryAreaAdjustmentRejectRequest request
    ) {
        ShopDeliveryAreaAdjustmentRejectCommand command = request.toCommand(requestId);
        shopDeliveryAreaAdjustmentCommandUseCase.rejectAdjustment(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
