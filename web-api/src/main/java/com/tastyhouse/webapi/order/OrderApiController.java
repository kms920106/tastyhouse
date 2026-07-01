package com.tastyhouse.webapi.order;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.core.domain.order.application.OrderCommandService;
import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.domain.order.application.dto.command.CreateOrderCommand;
import com.tastyhouse.core.domain.order.application.dto.command.CreateOrderProductCommand;
import com.tastyhouse.core.domain.order.application.dto.command.CreateOrderProductOptionCommand;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderCreateRequest;
import com.tastyhouse.webapi.order.response.OrderCreateResponse;
import com.tastyhouse.webapi.order.response.OrderProductResponse;
import com.tastyhouse.webapi.order.response.OrderDetailResponse;
import com.tastyhouse.webapi.order.response.PaymentSummaryResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.service.CustomUserDetails;
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

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 API")
public class OrderApiController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

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
        CreateOrderCommand command = toCreateOrderCommand(request);
        OrderResult result = orderCommandService.createOrder(userDetails.getMemberId(), command);
        return ResponseEntity.ok(ApiResponse.success(OrderCreateResponse.from(result.id())));
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
        com.tastyhouse.core.shared.page.PageResult<OrderListItemResult> page =
            orderQueryService.findOrderList(memberId, pageRequest.page(), pageRequest.size());
        List<OrderListItemResponse> items = page.content().stream()
            .map(this::toOrderListItemResponse)
            .toList();
        ApiResponse<List<OrderListItemResponse>> response = ApiResponse.success(
            items,
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
        OrderResult result = orderQueryService.findOrderDetail(memberId, orderId);
        return ResponseEntity.ok(ApiResponse.success(toOrderResponse(result, memberId)));
    }

    private OrderListItemResponse toOrderListItemResponse(OrderListItemResult dto) {
        return OrderListItemResponse.from(
            dto.id(),
            dto.shopName(),
            fileService.getUrlByPath(dto.shopThumbnailImageFilePath()),
            dto.firstProductName(),
            dto.totalItemCount(),
            dto.amount(),
            dto.paymentStatus(),
            dto.paymentDate()
        );
    }

    private CreateOrderCommand toCreateOrderCommand(OrderCreateRequest request) {
        List<CreateOrderProductCommand> itemCommands = request.orderProducts().stream()
            .map(product -> {
                List<CreateOrderProductOptionCommand> optionCommands = product.options() == null ? null :
                    product.options().stream()
                        .map(opt -> new CreateOrderProductOptionCommand(opt.groupId(), opt.optionId()))
                        .toList();
                return new CreateOrderProductCommand(product.productId(), product.quantity(), optionCommands);
            })
            .toList();
        return new CreateOrderCommand(
            request.shopId(),
            request.orderMethod(),
            itemCommands,
            request.memberCouponId(),
            request.usePoint(),
            request.totalProductAmount(),
            request.totalDiscountAmount(),
            request.productDiscountAmount(),
            request.couponDiscountAmount(),
            request.finalAmount()
        );
    }

    private OrderDetailResponse toOrderResponse(OrderResult result, Long memberId) {
        List<OrderProductResponse> orderProductsResponse = result.orderProducts().stream()
            .map(orderProduct -> {
                boolean reviewed = reviewQueryService.isReviewedByOrderAndProduct(
                    result.id(),
                    orderProduct.productId(),
                    memberId
                );
                String imageUrl = fileService.getUrlByPath(orderProduct.imageUrl());
                return OrderProductResponse.from(orderProduct, imageUrl, reviewed);
            })
            .toList();

        PaymentSummaryResponse paymentSummary = null;
        if (result.payment() != null) {
            paymentSummary = PaymentSummaryResponse.from(
                result.payment().id(),
                result.payment().paymentMethod(),
                result.payment().paymentStatus(),
                result.payment().amount(),
                result.payment().cardCompany(),
                result.payment().cardNumber(),
                result.payment().approvedAt(),
                result.payment().receiptUrl()
            );
        }

        return OrderDetailResponse.from(
            result.id(),
            result.orderNumber(),
            result.orderMethod(),
            result.paymentStatus(),
            result.shopName(),
            result.shopPhoneNumber(),
            result.ordererName(),
            result.ordererPhone(),
            result.ordererEmail(),
            result.totalProductAmount(),
            result.productDiscountAmount(),
            result.couponDiscountAmount(),
            result.pointDiscountAmount(),
            result.totalDiscountAmount(),
            result.finalAmount(),
            result.usedPoint(),
            result.earnedPoint(),
            orderProductsResponse,
            paymentSummary,
            result.approvedAt(),
            result.createdAt()
        );
    }
}
