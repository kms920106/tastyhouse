package com.tastyhouse.application.order.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.application.order.port.in.OrderDetailQueryUseCase;
import com.tastyhouse.application.order.port.out.OrderDetailResult;
import com.tastyhouse.application.order.port.out.OrderDetailViewResult;
import com.tastyhouse.application.order.port.out.OrderPaymentResult;
import com.tastyhouse.application.order.port.out.OrderPaymentSummaryResult;
import com.tastyhouse.application.order.port.out.OrderProductResult;
import com.tastyhouse.application.order.port.out.OrderProductViewResult;
import com.tastyhouse.application.order.port.out.OrderQueryPort;
import com.tastyhouse.application.review.port.in.ReviewWrittenProductIdsQueryUseCase;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class OrderDetailQueryService implements OrderDetailQueryUseCase {

    private final OrderQueryPort orderQueryPort;
    private final ReviewWrittenProductIdsQueryUseCase reviewWrittenProductIdsQueryUseCase;

    public OrderDetailQueryService(
        OrderQueryPort orderQueryPort,
        ReviewWrittenProductIdsQueryUseCase reviewWrittenProductIdsQueryUseCase
    ) {
        this.orderQueryPort = orderQueryPort;
        this.reviewWrittenProductIdsQueryUseCase = reviewWrittenProductIdsQueryUseCase;
    }

    @Override
    public OrderDetailViewResult getOrderDetail(Long memberId, Long orderId) {
        OrderDetailResult result = orderQueryPort.findOrderDetail(OrderId.of(orderId).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.ORDER_NOT_FOUND));

        if (!memberId.equals(result.memberId())) {
            throw new DomainException(DomainErrorCode.ORDER_ACCESS_DENIED);
        }

        return toOrderDetailViewResult(result, memberId);
    }

    private OrderDetailViewResult toOrderDetailViewResult(OrderDetailResult result, Long memberId) {

        List<Long> productIds = result.orderProducts().stream()
            .map(OrderProductResult::productId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Set<Long> reviewedProductIds = reviewWrittenProductIdsQueryUseCase.findReviewedProductIds(result.id(), memberId, productIds);

        List<OrderProductViewResult> orderProducts = result.orderProducts().stream()
            .map(orderProduct -> toOrderProductViewResult(orderProduct, reviewedProductIds))
            .toList();

        OrderPaymentSummaryResult payment = result.payment() != null
            ? toOrderPaymentSummaryResult(result.payment(), result.cupDepositAmount())
            : null;

        return new OrderDetailViewResult(
            result.id(),
            result.orderNumber(),
            result.orderMethod(),
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
        return payment.paymentStatus();
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
            result.paymentMethod(),
            result.paymentStatus(),
            result.amount(),
            cupDepositAmount,
            result.cardCompany(),
            result.cardNumber(),
            result.approvedAt(),
            result.receiptUrl()
        );
    }
}
