package com.tastyhouse.webapi.order;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.core.domain.order.application.OrderCommandService;
import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.domain.order.application.dto.command.CreateOrderCommand;
import com.tastyhouse.core.domain.order.application.dto.command.CreateOrderItemCommand;
import com.tastyhouse.core.domain.order.application.dto.command.CreateOrderItemOptionCommand;
import com.tastyhouse.core.domain.order.application.dto.result.OrderItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderCreateRequest;
import com.tastyhouse.webapi.order.response.OrderItemOptionResponse;
import com.tastyhouse.webapi.order.response.OrderItemResponse;
import com.tastyhouse.webapi.order.response.OrderResponse;
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 생성 성공", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
        @Valid @RequestBody OrderCreateRequest request,
        @CurrentUser CustomUserDetails userDetails
    ) {
        CreateOrderCommand command = toCreateOrderCommand(request);
        OrderResult result = orderCommandService.createOrder(userDetails.getMemberId(), command);
        return ResponseEntity.ok(ApiResponse.success(toOrderResponse(result, userDetails.getMemberId())));
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
        org.springframework.data.domain.Page<OrderListItemResult> page =
            orderQueryService.findOrderList(memberId, pageRequest.page(), pageRequest.size());
        List<OrderListItemResponse> items = page.getContent().stream()
            .map(this::toOrderListItemResponse)
            .toList();
        ApiResponse<List<OrderListItemResponse>> response = ApiResponse.success(
            items,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주문 상세 조회", description = "주문 상세 정보를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 주문이 아닌 경우)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    @GetMapping("/v1/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
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
        List<CreateOrderItemCommand> itemCommands = request.orderItems().stream()
            .map(item -> {
                List<CreateOrderItemOptionCommand> optionCommands = item.selectedOptions() == null ? null :
                    item.selectedOptions().stream()
                        .map(opt -> new CreateOrderItemOptionCommand(opt.groupId(), opt.optionId()))
                        .toList();
                return new CreateOrderItemCommand(item.productId(), item.quantity(), optionCommands);
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

    private OrderResponse toOrderResponse(OrderResult result, Long memberId) {
        List<OrderItemResponse> itemResponses = result.orderItems().stream()
            .map(item -> toOrderItemResponse(item, result.id(), memberId))
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

        return OrderResponse.from(
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
            itemResponses,
            paymentSummary,
            result.approvedAt(),
            result.createdAt()
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItemResult item, Long orderId, Long memberId) {
        List<OrderItemOptionResponse> optionResponses = item.options().stream()
            .map(opt -> OrderItemOptionResponse.from(
                opt.id(),
                opt.optionGroupName(),
                opt.optionName(),
                opt.additionalPrice()
            ))
            .toList();

        boolean isReviewed = reviewQueryService.existsReviewByOrderItemAndMember(
            orderId, item.productId(), memberId
        );

        return OrderItemResponse.from(
            item.id(),
            item.productId(),
            item.productName(),
            fileService.getUrlByPath(item.productImageFilePath()),
            item.quantity(),
            item.unitPrice(),
            item.discountPrice(),
            item.optionTotalPrice(),
            item.totalPrice(),
            isReviewed,
            optionResponses
        );
    }
}
