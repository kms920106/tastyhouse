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

/**
 * 회원 주문 조회 서비스(web-api).
 *
 * <p>infra query DAO({@link OrderQueryPort})만 주입해 조회하고, 읽기 계약 조립(private 매퍼)을
 * 담당한다(공통 지침 패턴 2·3). write 포트는 주입하지 않는다. 표현 계약({@code Order*Response})
 * 조립은 web-api가 담당한다(챕터 10).
 *
 * <p>주문 상세는 회원 스코프 조회이므로, DAO가 함께 투영한 {@code memberId}를 요청 회원과 대조해 남의
 * 주문 열람을 막는다(도메인 모델 {@code Order#validateOwnership}과 동일한 {@code ORDER_ACCESS_DENIED}).
 */
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

    /**
     * 내 주문 목록.
     */
    @Override
    public PageResult<OrderListItemResult> getOrderList(Long memberId, int page, int size) {
        return orderQueryPort.findOrders(MemberId.of(memberId), PageQuery.of(page, size));
    }

    /**
     * 내 주문 상세 — 요청 회원의 주문이 아니면 {@code ORDER_ACCESS_DENIED}.
     */
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
        // 주문상품마다 리뷰 여부를 개별 조회하면 상품 수만큼 쿼리가 나가므로(N+1), 상품 식별자를 모아
        // 한 번에 조회하고 아래 매핑 루프는 메모리에서 판정한다.
        List<Long> productIds = result.orderProducts().stream()
            .map(OrderProductResult::productId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Set<Long> reviewedProductIds = reviewQueryService.findReviewedProductIds(result.id(), memberId, productIds);

        List<OrderProductViewResult> orderProducts = result.orderProducts().stream()
            .map(orderProduct -> toOrderProductViewResult(orderProduct, reviewedProductIds))
            .toList();

        // 보증금은 결제(PAYMENT) 테이블이 아니라 주문에 저장된다 — PAYMENT.amount는 손님이 실제로 내는
        // 돈(보증금 포함 final_amount)이고, 그중 얼마가 보증금인지는 주문이 안다. 그래서 결제 요약에
        // 넣을 값도 주문에서 가져온다(PAYMENT 스키마·모델은 변경하지 않는다).
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

    /**
     * 결제 상태 이름 — 결제가 없거나 상태가 비어 있으면 {@code null}(기존 동작 보존).
     */
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
