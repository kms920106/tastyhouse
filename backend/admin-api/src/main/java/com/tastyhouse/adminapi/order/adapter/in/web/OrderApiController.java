package com.tastyhouse.adminapi.order.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.tastyhouse.adminapi.order.adapter.in.web.request.OrderSearchRequest;
import com.tastyhouse.adminapi.order.adapter.in.web.request.OrderStatusUpdateRequest;
import com.tastyhouse.adminapplication.order.response.OrderDetailResponse;
import com.tastyhouse.adminapplication.order.response.OrderListItemResponse;
import com.tastyhouse.adminapplication.order.port.in.OrderCommandUseCase;
import com.tastyhouse.adminapplication.order.port.in.OrderDeleteCommand;
import com.tastyhouse.adminapplication.order.port.in.OrderStatusChangeCommand;
import com.tastyhouse.adminapplication.order.port.in.OrderQueryUseCase;

@Tag(name = "Order Admin", description = "주문 관리자 API")
@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderCommandUseCase orderCommandUseCase;
    private final OrderQueryUseCase orderQueryUseCase;

    public OrderApiController(OrderCommandUseCase orderCommandUseCase, OrderQueryUseCase orderQueryUseCase) {
        this.orderCommandUseCase = orderCommandUseCase;
        this.orderQueryUseCase = orderQueryUseCase;
    }

    @Operation(summary = "주문 목록 조회", description = "주문 목록을 페이징 조회합니다. 가게/주문상태/주문방법/결제상태/주문번호/주문자명/기간 필터를 지원합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<OrderListItemResponse>>> getOrders(
        @Valid @ModelAttribute OrderSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<OrderListItemResponse> pageResponse = orderQueryUseCase.getOrders(
            search.shopId(), search.orderStatus(), search.orderMethod(), search.paymentStatus(),
            search.orderNumber(), search.ordererName(), search.startDate(), search.endDate(),
            pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(@PathVariable Long id) {
        OrderDetailResponse response = orderQueryUseCase.getOrder(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문 상태 변경", description = "주문 상태를 변경합니다.")
    @PatchMapping("/v1/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
        @PathVariable Long id,
        @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderStatusChangeCommand command = request.toCommand(id);
        orderCommandUseCase.changeStatus(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "주문 삭제", description = "주문을 삭제합니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        OrderDeleteCommand command = OrderDeleteCommand.of(id);
        orderCommandUseCase.deleteOrder(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
