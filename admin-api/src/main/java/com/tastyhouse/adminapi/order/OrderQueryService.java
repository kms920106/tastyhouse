package com.tastyhouse.adminapi.order;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.order.domain.model.OrderStatus;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.order.query.OrderDetailResult;
import com.tastyhouse.infrastructure.order.query.OrderManagementListItemResult;
import com.tastyhouse.infrastructure.order.query.OrderPaymentResult;
import com.tastyhouse.infrastructure.order.query.OrderProductOptionResult;
import com.tastyhouse.infrastructure.order.query.OrderProductResult;
import com.tastyhouse.infrastructure.order.query.OrderQueryDao;
import com.tastyhouse.infrastructure.order.query.OrderSearchCondition;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.order.response.OrderDetailResponse;
import com.tastyhouse.adminapi.order.response.OrderListItemResponse;
import com.tastyhouse.adminapi.order.response.OrderProductOptionResponse;
import com.tastyhouse.adminapi.order.response.OrderProductResponse;
import com.tastyhouse.adminapi.order.response.PaymentSummaryResponse;

/**
 * 주문 관리 조회 서비스(admin-api).
 *
 * <p>infra query DAO({@link OrderQueryDao})만 주입해 조회하고, 응답 조립(private 매퍼)을 담당한다
 * (공통 지침 패턴 2·3). write 포트는 주입하지 않는다.
 *
 * <p>enum 후보값은 HTTP 경계에서 {@code String}으로 받아 여기서 {@code Enum.from(...)}으로 승격한다
 * (도메인 enum 경계 규칙). 관리자 조회는 회원 스코프가 없어 소유권 검증을 하지 않는다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderQueryDao orderQueryDao;

    /**
     * 주문 관리 목록.
     */
    public PaginationResponse<OrderListItemResponse> getOrders(
        Long shopId,
        String orderStatus,
        String orderMethod,
        String paymentStatus,
        String orderNumber,
        String ordererName,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
    ) {
        OrderSearchCondition condition = OrderSearchCondition.of(
            shopId,
            orderStatus == null ? null : OrderStatus.from(orderStatus),
            orderMethod == null ? null : OrderMethod.from(orderMethod),
            paymentStatus == null ? null : PaymentStatus.valueOf(paymentStatus),
            orderNumber,
            ordererName,
            startDate,
            endDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<OrderListItemResponse> pageResult = orderQueryDao.findOrders(condition, pageQuery)
            .map(this::toOrderListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 주문 관리 상세.
     */
    public OrderDetailResponse getOrder(Long id) {
        OrderDetailResult result = orderQueryDao.findOrderDetail(OrderId.of(id))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        return toOrderDetailResponse(result);
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

    private OrderDetailResponse toOrderDetailResponse(OrderDetailResult result) {
        List<OrderProductResponse> orderProducts = result.orderProducts().stream()
            .map(this::toOrderProductResponse)
            .toList();
        PaymentSummaryResponse payment = result.payment() != null ? toPaymentSummaryResponse(result.payment()) : null;
        return OrderDetailResponse.from(
            result.id(),
            result.orderNumber(),
            result.orderMethod() != null ? result.orderMethod().name() : null,
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
            result.finalAmount(),
            result.usedPoint(),
            result.earnedPoint(),
            orderProducts,
            payment,
            result.payment() == null ? null : result.payment().approvedAt(),
            result.createdAt()
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

    private OrderProductResponse toOrderProductResponse(OrderProductResult result) {
        List<OrderProductOptionResponse> selectedOptions = result.options().stream()
            .map(this::toOrderProductOptionResponse)
            .toList();
        return OrderProductResponse.from(
            result.orderProductId(),
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
            result.orderProductOptionId(),
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
            result.amount() != null ? result.amount().value() : null,
            result.cardCompany(),
            result.cardNumber(),
            result.approvedAt(),
            result.receiptUrl()
        );
    }
}
