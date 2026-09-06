package com.tastyhouse.application.order.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.order.port.out.OrderDetailResult;
import com.tastyhouse.application.order.port.out.OrderListItemResult;
import com.tastyhouse.application.order.port.out.OrderPaymentResult;
import com.tastyhouse.application.order.port.out.OrderProductResult;
import com.tastyhouse.application.order.port.out.OrderQueryPort;
import com.tastyhouse.application.order.port.in.OrderQueryUseCase;
import com.tastyhouse.application.order.port.out.OrderDetailViewResult;
import com.tastyhouse.application.order.port.out.OrderPaymentSummaryResult;
import com.tastyhouse.application.order.port.out.OrderProductViewResult;
import com.tastyhouse.application.review.service.ReviewQueryService;

@Service
@WebApp
@Transactional(readOnly = true)
public class OrderQueryService implements OrderQueryUseCase {

    private final OrderQueryPort orderQueryPort;
    private final ReviewQueryService reviewQueryService;

    public OrderQueryService(OrderQueryPort orderQueryPort, ReviewQueryService reviewQueryService) {
        this.orderQueryPort = orderQueryPort;
        this.reviewQueryService = reviewQueryService;
    }

    @Override
    public PageResult<OrderListItemResult> getOrderList(Long memberId, int page, int size) {
        return orderQueryPort.findOrders(MemberId.of(memberId), PageQuery.of(page, size));
    }

    @Override
    public OrderDetailViewResult getOrderDetail(Long memberId, Long orderId) {
        OrderDetailResult result = orderQueryPort.findOrderDetail(OrderId.of(orderId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!memberId.equals(result.memberId())) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return toOrderDetailViewResult(result, memberId);
    }

    private OrderDetailViewResult toOrderDetailViewResult(OrderDetailResult result, Long memberId) {

        List<Long> productIds = result.orderProducts().stream()
            .map(OrderProductResult::productId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Set<Long> reviewedProductIds = reviewQueryService.findReviewedProductIds(result.id(), memberId, productIds);

        List<OrderProductViewResult> orderProducts = result.orderProducts().stream()
            .map(orderProduct -> toOrderProductViewResult(orderProduct, reviewedProductIds))
            .toList();

        OrderPaymentSummaryResult payment = result.payment() != null
            ? toOrderPaymentSummaryResult(result.payment(), result.cupDepositAmount())
            : null;

        return new OrderDetailViewResult(
            result.id(),
            result.orderNumber(),
            result.orderMethod().name(),
            toPaymentStatusName(result.payment()),
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
            result.cupDepositAmount(),
            result.finalAmount(),
            result.usedPoint(),
            result.earnedPoint(),
            orderProducts,
            payment,
            result.payment() == null ? null : result.payment().approvedAt(),
            result.createdAt(),
            result.scheduledAt(),
            result.scheduledSlotEndAt()
        );
    }

    private String toPaymentStatusName(OrderPaymentResult payment) {
        if (payment == null || payment.paymentStatus() == null) {
            return null;
        }
        return payment.paymentStatus().name();
    }

    private OrderProductViewResult toOrderProductViewResult(OrderProductResult result, Set<Long> reviewedProductIds) {
        boolean reviewed = reviewedProductIds.contains(result.productId());
        return new OrderProductViewResult(
            result.orderProductId(),
            result.productId(),
            result.name(),
            result.priceName(),
            result.imageUrl(),
            result.quantity(),
            result.originalPrice(),
            result.discountPrice(),
            result.totalOptionPrice(),
            result.totalPrice(),
            result.options(),
            reviewed
        );
    }

    private OrderPaymentSummaryResult toOrderPaymentSummaryResult(
        OrderPaymentResult result,
        Integer cupDepositAmount
    ) {
        return new OrderPaymentSummaryResult(
            result.id(),
            result.paymentMethod() != null ? result.paymentMethod().name() : null,
            result.paymentStatus() != null ? result.paymentStatus().name() : null,
            result.amount(),
            cupDepositAmount,
            result.cardCompany(),
            result.cardNumber(),
            result.approvedAt(),
            result.receiptUrl()
        );
    }
}
