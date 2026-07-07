package com.tastyhouse.webapi.order;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.config.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderCreateRequest;
import com.tastyhouse.webapi.order.response.OrderCreateResponse;
import com.tastyhouse.webapi.order.response.OrderDetailResponse;
import com.tastyhouse.webapi.order.response.OrderListPageResult;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 API")
public class OrderApiController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 생성 성공", content = @Content(schema = @Schema(implementation = OrderCreateResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
        @Valid @RequestBody OrderCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        Long orderId = orderService.createOrder(
            userDetails.getMemberId(),
            request.shopId(),
            request.orderMethod(),
            request.orderProducts(),
            request.memberCouponId(),
            request.usePoint(),
            request.totalProductAmount(),
            request.totalDiscountAmount(),
            request.productDiscountAmount(),
            request.couponDiscountAmount(),
            request.finalAmount()
        );
        return ResponseEntity.ok(ApiResponse.success(OrderCreateResponse.from(orderId)));
    }

    @Operation(summary = "주문 목록 조회", description = "회원의 주문 목록을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<OrderListItemResponse>>> getOrderList(
        @CurrentUser CustomUserDetails userDetails,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        Long memberId = userDetails.getMemberId();
        OrderListPageResult page = orderService.getOrderList(memberId, pageRequest.page(), pageRequest.size());
        ApiResponse<List<OrderListItemResponse>> response = ApiResponse.success(
            page.content(),
            page.page(),
            page.size(),
            page.totalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = OrderDetailResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 주문이 아닌 경우)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @GetMapping("/v1/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
        @PathVariable Long orderId,
        @CurrentUser CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        OrderDetailResponse response = orderService.getOrderDetail(memberId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
