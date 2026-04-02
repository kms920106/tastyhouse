package com.tastyhouse.webapi.order;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderCreateRequest;
import com.tastyhouse.webapi.order.response.OrderResponse;
import com.tastyhouse.webapi.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 API")
public class OrderApiController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 생성 성공", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/v1")
    public ResponseEntity<CommonResponse<OrderResponse>> createOrder(
        @Valid @RequestBody OrderCreateRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        OrderResponse response = orderService.createOrder(userDetails.getMemberId(), request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "주문 목록 조회", description = "회원의 주문 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/v1")
    public ResponseEntity<CommonResponse<List<OrderListItemResponse>>> getOrderList(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        Long memberId = userDetails.getMemberId();
        PageResult<OrderListItemResponse> pageResult = orderService.getOrderList(memberId, pageRequest);
        CommonResponse<List<OrderListItemResponse>> response = CommonResponse.success(
            pageResult.getContent(),
            pageResult.getCurrentPage(),
            pageResult.getPageSize(),
            pageResult.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 주문이 아닌 경우)"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @GetMapping("/v1/{orderId}")
    public ResponseEntity<CommonResponse<OrderResponse>> getOrderDetail(
        @PathVariable Long orderId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        OrderResponse response = orderService.getOrderDetail(userDetails.getMemberId(), orderId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

}
