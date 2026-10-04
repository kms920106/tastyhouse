package com.tastyhouse.adminapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.in.ShopManagementQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodAssignUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodUnassignUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.ShopOrderMethodAssignRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopOrderMethodItemResponse;

@Tag(name = "Shop Order Method Admin", description = "가게 주문수단 관리자 API")
@RestController
@RequestMapping("/api/shops")
class ShopOrderMethodAdminApiController {

    private final ShopOrderMethodAssignUseCase shopOrderMethodAssignUseCase;
    private final ShopOrderMethodUnassignUseCase shopOrderMethodUnassignUseCase;
    private final ShopManagementQueryUseCase shopQueryUseCase;

    public ShopOrderMethodAdminApiController(
        ShopOrderMethodAssignUseCase shopOrderMethodAssignUseCase,
        ShopOrderMethodUnassignUseCase shopOrderMethodUnassignUseCase,
        ShopManagementQueryUseCase shopQueryUseCase
    ) {
        this.shopOrderMethodAssignUseCase = shopOrderMethodAssignUseCase;
        this.shopOrderMethodUnassignUseCase = shopOrderMethodUnassignUseCase;
        this.shopQueryUseCase = shopQueryUseCase;
    }

    @Operation(summary = "가게 주문수단 목록 조회", description = "가게에 지정된 주문수단 목록을 조회합니다.")
    @GetMapping("/v1/{id}/order-methods")
    public ResponseEntity<ApiResponse<List<ShopOrderMethodItemResponse>>> getOrderMethods(@PathVariable Long id) {
        List<ShopOrderMethodItemResponse> response = shopQueryUseCase.getOrderMethods(id).stream()
            .map(ShopOrderMethodItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가게 주문수단 지정", description = "가게에 주문수단을 지정합니다.")
    @PostMapping("/v1/{id}/order-methods")
    public ResponseEntity<ApiResponse<Long>> assignOrderMethod(
        @PathVariable Long id,
        @Valid @RequestBody ShopOrderMethodAssignRequest request
    ) {
        ShopOrderMethodAssignCommand command = request.toCommand(id);
        Long orderMethodId = shopOrderMethodAssignUseCase.assignOrderMethod(command);
        return ResponseEntity.ok(ApiResponse.success(orderMethodId));
    }

    @Operation(summary = "가게 주문수단 해제", description = "가게에 지정된 주문수단을 해제합니다.")
    @DeleteMapping("/v1/{id}/order-methods/{orderMethod}")
    public ResponseEntity<ApiResponse<Void>> unassignOrderMethod(
        @PathVariable Long id,
        @PathVariable String orderMethod
    ) {
        ShopOrderMethodUnassignCommand command = ShopOrderMethodUnassignCommand.of(id, orderMethod);
        shopOrderMethodUnassignUseCase.unassignOrderMethod(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
