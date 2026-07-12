package com.tastyhouse.webapi.order;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.order.application.OrderCommandService;
import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.domain.order.application.dto.command.OrderCreateCommand;
import com.tastyhouse.core.domain.order.application.dto.command.OrderProductCreateCommand;
import com.tastyhouse.core.domain.order.application.dto.command.OrderProductOptionCreateCommand;
import com.tastyhouse.core.domain.order.application.dto.result.OrderListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.request.OrderProductRequest;
import com.tastyhouse.webapi.order.response.OrderDetailResponse;
import com.tastyhouse.webapi.order.response.OrderListPageResult;
import com.tastyhouse.webapi.order.response.OrderProductResponse;
import com.tastyhouse.webapi.order.response.PaymentSummaryResponse;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    public Long createOrder(
        Long memberId,
        Long shopId,
        String orderMethod,
        List<OrderProductRequest> orderProducts,
        Long memberCouponId,
        Integer usePoint,
        Integer totalProductAmount,
        Integer totalDiscountAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer finalAmount
    ) {
        OrderCreateCommand command = toCreateOrderCommand(
            shopId,
            OrderMethod.from(orderMethod),
            orderProducts,
            memberCouponId,
            usePoint,
            totalProductAmount,
            totalDiscountAmount,
            productDiscountAmount,
            couponDiscountAmount,
            finalAmount
        );
        OrderResult result = orderCommandService.createOrder(memberId, command);
        return result.orderId().value();
    }

    public OrderListPageResult getOrderList(Long memberId, int page, int size) {
        PageResult<OrderListItemResponse> pageResult = orderQueryService.findOrderList(memberId, page, size)
            .map(this::toOrderListItemResponse);
        return new OrderListPageResult(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }

    public OrderDetailResponse getOrderDetail(Long memberId, Long orderId) {
        OrderResult result = orderQueryService.findOrderDetail(memberId, OrderId.of(orderId));
        return toOrderResponse(result, memberId);
    }

    private OrderListItemResponse toOrderListItemResponse(OrderListItemResult dto) {
        return OrderListItemResponse.from(
            dto.id(),
            dto.shopName(),
            fileService.getUrlByPath(dto.shopThumbnailImageFilePath()),
            dto.firstProductName(),
            dto.totalItemCount(),
            dto.amount(),
            dto.paymentStatus().name(),
            dto.paymentDate()
        );
    }

    private OrderCreateCommand toCreateOrderCommand(
        Long shopId,
        OrderMethod orderMethod,
        List<OrderProductRequest> orderProducts,
        Long memberCouponId,
        Integer usePoint,
        Integer totalProductAmount,
        Integer totalDiscountAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer finalAmount
    ) {
        List<OrderProductCreateCommand> itemCommands = orderProducts.stream()
            .map(product -> {
                List<OrderProductOptionCreateCommand> optionCommands = product.options() == null ? null :
                    product.options().stream()
                        .map(opt -> OrderProductOptionCreateCommand.of(opt.groupId(), opt.optionId()))
                        .toList();
                return OrderProductCreateCommand.of(product.productId(), product.quantity(), optionCommands);
            })
            .toList();
        return OrderCreateCommand.of(
            shopId,
            orderMethod,
            itemCommands,
            memberCouponId,
            usePoint,
            totalProductAmount,
            totalDiscountAmount,
            productDiscountAmount,
            couponDiscountAmount,
            finalAmount
        );
    }

    private OrderDetailResponse toOrderResponse(OrderResult result, Long memberId) {
        List<OrderProductResponse> orderProductsResponse = result.orderProducts().stream()
            .map(orderProduct -> {
                boolean reviewed = reviewQueryService.isReviewedByOrderAndProduct(
                    result.orderId().value(),
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
                result.payment().paymentMethod().name(),
                result.payment().paymentStatus().name(),
                result.payment().amount(),
                result.payment().cardCompany(),
                result.payment().cardNumber(),
                result.payment().approvedAt(),
                result.payment().receiptUrl()
            );
        }

        return OrderDetailResponse.from(
            result.orderId().value(),
            result.orderNumber(),
            result.orderMethod().name(),
            result.paymentStatus() == null ? null : result.paymentStatus().name(),
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
