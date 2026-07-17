package com.tastyhouse.adminapi.order;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.order.domain.model.OrderStatus;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.order.application.OrderCommandService;
import com.tastyhouse.core.domain.order.application.OrderQueryService;
import com.tastyhouse.core.domain.order.application.dto.OrderSearchCondition;
import com.tastyhouse.core.domain.order.application.dto.result.OrderManagementListItemResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderPaymentResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderProductOptionResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderProductResult;
import com.tastyhouse.core.domain.order.application.dto.result.OrderResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.order.response.OrderDetailResponse;
import com.tastyhouse.adminapi.order.response.OrderListItemResponse;
import com.tastyhouse.adminapi.order.response.OrderPageResponse;
import com.tastyhouse.adminapi.order.response.OrderProductOptionResponse;
import com.tastyhouse.adminapi.order.response.OrderProductResponse;
import com.tastyhouse.adminapi.order.response.PaymentSummaryResponse;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    public OrderPageResponse getOrders(Long shopId, String orderStatus, String orderMethod, String paymentStatus,
                                       String orderNumber, String ordererName,
                                       LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        OrderStatus status = orderStatus == null ? null : OrderStatus.from(orderStatus);
        OrderMethod method = orderMethod == null ? null : OrderMethod.from(orderMethod);
        PaymentStatus payment = paymentStatus == null ? null : PaymentStatus.valueOf(paymentStatus);
        OrderSearchCondition condition = OrderSearchCondition.of(shopId, status, method, payment, orderNumber, ordererName, startDate, endDate);
        PageResult<OrderListItemResponse> pageResult = orderQueryService.findOrders(condition, page, size)
            .map(this::toOrderListItemResponse);
        return OrderPageResponse.from(pageResult);
    }

    public OrderDetailResponse getOrder(Long id) {
        OrderId orderId = OrderId.of(id);
        OrderResult result = orderQueryService.findOrderDetailById(orderId);
        return toOrderDetailResponse(result);
    }

    public void changeStatus(Long id, String status) {
        OrderId orderId = OrderId.of(id);
        OrderStatus orderStatus = OrderStatus.from(status);
        orderCommandService.changeOrderStatus(orderId, orderStatus);
    }

    public void deleteOrder(Long id) {
        OrderId orderId = OrderId.of(id);
        orderCommandService.deleteOrder(orderId);
    }

    private OrderListItemResponse toOrderListItemResponse(OrderManagementListItemResult result) {
        return OrderListItemResponse.from(
            result.id(),
            result.orderNumber(),
            result.shopName(),
            result.ordererName(),
            result.orderMethod() != null ? result.orderMethod().name() : null,
            result.orderStatus() != null ? result.orderStatus().name() : null,
            result.paymentStatus() != null ? result.paymentStatus().name() : null,
            result.finalAmount(),
            result.totalItemCount(),
            result.createdAt()
        );
    }

    private OrderDetailResponse toOrderDetailResponse(OrderResult result) {
        List<OrderProductResponse> orderProducts = result.orderProducts() == null ? List.of() :
            result.orderProducts().stream()
                .map(this::toOrderProductResponse)
                .toList();
        PaymentSummaryResponse payment = result.payment() != null ? toPaymentSummaryResponse(result.payment()) : null;
        return OrderDetailResponse.from(
            result.orderId().value(),
            result.orderNumber(),
            result.orderMethod() != null ? result.orderMethod().name() : null,
            result.paymentStatus() != null ? result.paymentStatus().name() : null,
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
            orderProducts,
            payment,
            result.approvedAt(),
            result.createdAt()
        );
    }

    private OrderProductResponse toOrderProductResponse(OrderProductResult result) {
        List<OrderProductOptionResponse> selectedOptions = result.options() == null ? List.of() :
            result.options().stream()
                .map(this::toOrderProductOptionResponse)
                .toList();
        return OrderProductResponse.from(
            result.orderProductId().value(),
            result.productId(),
            result.name(),
            result.imageUrl(),
            result.quantity(),
            result.originalPrice(),
            result.discountPrice(),
            result.totalOptionPrice(),
            result.totalPrice(),
            selectedOptions
        );
    }

    private OrderProductOptionResponse toOrderProductOptionResponse(OrderProductOptionResult result) {
        return OrderProductOptionResponse.from(
            result.orderProductOptionId().value(),
            result.optionGroupName(),
            result.optionName(),
            result.additionalPrice()
        );
    }

    private PaymentSummaryResponse toPaymentSummaryResponse(OrderPaymentResult result) {
        return PaymentSummaryResponse.from(
            result.id(),
            result.paymentMethod() != null ? result.paymentMethod().name() : null,
            result.paymentStatus() != null ? result.paymentStatus().name() : null,
            result.amount(),
            result.cardCompany(),
            result.cardNumber(),
            result.approvedAt(),
            result.receiptUrl()
        );
    }
}
