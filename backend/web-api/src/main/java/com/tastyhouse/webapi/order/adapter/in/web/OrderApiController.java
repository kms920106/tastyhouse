package com.tastyhouse.webapi.order.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.order.port.in.OrderCreateCommand;
import com.tastyhouse.application.order.port.in.OrderCreateUseCase;
import com.tastyhouse.application.order.port.in.OrderDetailQueryUseCase;
import com.tastyhouse.application.order.port.in.OrderListQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.member.adapter.in.web.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.adapter.in.web.request.OrderCreateRequest;
import com.tastyhouse.webapi.order.adapter.in.web.response.OrderDetailResponse;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "주문 API")
class OrderApiController {

    private final OrderCreateUseCase orderCreateUseCase;
    private final OrderListQueryUseCase orderListQueryUseCase;
    private final OrderDetailQueryUseCase orderDetailQueryUseCase;

    public OrderApiController(
        OrderCreateUseCase orderCreateUseCase,
        OrderListQueryUseCase orderListQueryUseCase,
        OrderDetailQueryUseCase orderDetailQueryUseCase
    ) {
        this.orderCreateUseCase = orderCreateUseCase;
        this.orderListQueryUseCase = orderListQueryUseCase;
        this.orderDetailQueryUseCase = orderDetailQueryUseCase;
    }

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다. 생성된 주문 ID를 반환합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createOrder(
        @Valid @RequestBody OrderCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        OrderCreateCommand command = request.toCommand(userDetails.getMemberId());
        Long orderId = orderCreateUseCase.createOrder(command);
        return ResponseEntity.ok(ApiResponse.success(orderId));
    }

    @Operation(summary = "주문 목록 조회", description = "회원의 주문 목록을 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<OrderListItemResponse>>> getOrderList(
        @CurrentUser MemberUserDetails userDetails,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        Long memberId = userDetails.getMemberId();
        PaginationResponse<OrderListItemResponse> page = PaginationResponse.from(
            orderListQueryUseCase.getOrderList(memberId, pageRequest.page(), pageRequest.size())
                .map(OrderListItemResponse::from)
        );
        ApiResponse<List<OrderListItemResponse>> response = ApiResponse.success(
            page.content(),
            page.page(),
            page.size(),
            page.totalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
        @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        OrderDetailResponse response = OrderDetailResponse.from(orderDetailQueryUseCase.getOrderDetail(memberId, id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
