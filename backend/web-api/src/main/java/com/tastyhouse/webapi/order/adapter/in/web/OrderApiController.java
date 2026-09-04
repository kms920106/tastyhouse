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

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.auth.security.MemberUserDetails;
import com.tastyhouse.webapi.member.adapter.in.web.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.adapter.in.web.request.OrderCreateRequest;
import com.tastyhouse.webapi.order.adapter.in.web.response.OrderDetailResponse;
import com.tastyhouse.webapplication.order.port.in.OrderCommandUseCase;
import com.tastyhouse.webapplication.order.port.in.OrderCreateCommand;
import com.tastyhouse.webapplication.order.port.in.OrderQueryUseCase;
import com.tastyhouse.webapi.security.CurrentUser;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "주문 API")
public class OrderApiController {

    private final OrderCommandUseCase orderCommandUseCase;
    private final OrderQueryUseCase orderQueryService;

    public OrderApiController(OrderCommandUseCase orderCommandUseCase, OrderQueryUseCase orderQueryService) {
        this.orderCommandUseCase = orderCommandUseCase;
        this.orderQueryService = orderQueryService;
    }

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다. 생성된 주문 ID를 반환합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createOrder(
        @Valid @RequestBody OrderCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        OrderCreateCommand command = request.toCommand(userDetails.getMemberId());
        Long orderId = orderCommandUseCase.createOrder(command);
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
            orderQueryService.getOrderList(memberId, pageRequest.page(), pageRequest.size())
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
        OrderDetailResponse response = OrderDetailResponse.from(orderQueryService.getOrderDetail(memberId, id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
