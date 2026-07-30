package com.tastyhouse.adminapi.order;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.common.PageRequest;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.order.request.OrderSearchRequest;
import com.tastyhouse.adminapi.order.request.OrderStatusUpdateRequest;
import com.tastyhouse.adminapi.order.response.OrderDetailResponse;
import com.tastyhouse.adminapi.order.response.OrderListItemResponse;

@Tag(name = "Order Admin", description = "주문 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    @Operation(summary = "주문 목록 조회", description = "주문 목록을 페이징 조회합니다. 가게/주문상태/주문방법/결제상태/주문번호/주문자명/기간 필터를 지원합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<OrderListItemResponse>>> getOrders(
        @Valid @ModelAttribute OrderSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<OrderListItemResponse> pageResponse = orderQueryService.getOrders(
            search.shopId(), search.orderStatus(), search.orderMethod(), search.paymentStatus(),
            search.orderNumber(), search.ordererName(), search.startDate(), search.endDate(),
            pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(@PathVariable Long id) {
        OrderDetailResponse response = orderQueryService.getOrder(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문 상태 변경", description = "주문 상태를 변경합니다.")
    @PatchMapping("/v1/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
        @PathVariable Long id,
        @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        orderCommandService.changeStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "주문 삭제", description = "주문을 삭제합니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderCommandService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
