package com.tastyhouse.webapi.order;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.exception.AccessDeniedException;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.order.query.OrderDetailResult;
import com.tastyhouse.infrastructure.order.query.OrderListItemResult;
import com.tastyhouse.infrastructure.order.query.OrderPaymentResult;
import com.tastyhouse.infrastructure.order.query.OrderProductOptionResult;
import com.tastyhouse.infrastructure.order.query.OrderProductResult;
import com.tastyhouse.infrastructure.order.query.OrderQueryDao;
import com.tastyhouse.webapi.common.PaginationResponse;
import com.tastyhouse.webapi.file.FileService;
import com.tastyhouse.webapi.member.response.OrderListItemResponse;
import com.tastyhouse.webapi.order.response.OrderDetailResponse;
import com.tastyhouse.webapi.order.response.OrderProductOptionResponse;
import com.tastyhouse.webapi.order.response.OrderProductResponse;
import com.tastyhouse.webapi.order.response.PaymentSummaryResponse;
import com.tastyhouse.webapi.review.ReviewQueryService;

/**
 * 회원 주문 조회 서비스(web-api).
 *
 * <p>infra query DAO({@link OrderQueryDao})만 주입해 조회하고, 응답 조립(private 매퍼)을 담당한다
 * (공통 지침 패턴 2·3). write 포트는 주입하지 않는다.
 *
 * <p>주문 상세는 회원 스코프 조회이므로, DAO가 함께 투영한 {@code memberId}를 요청 회원과 대조해 남의
 * 주문 열람을 막는다(도메인 모델 {@code Order#validateOwnership}과 동일한 {@code ORDER_ACCESS_DENIED}).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderQueryDao orderQueryDao;
    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    /**
     * 내 주문 목록.
     */
    public PaginationResponse<OrderListItemResponse> getOrderList(Long memberId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<OrderListItemResponse> pageResult = orderQueryDao
            .findOrders(MemberId.of(memberId), pageQuery)
            .map(this::toOrderListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 내 주문 상세 — 요청 회원의 주문이 아니면 {@code ORDER_ACCESS_DENIED}.
     */
    public OrderDetailResponse getOrderDetail(Long memberId, Long orderId) {
        OrderDetailResult result = orderQueryDao.findOrderDetail(OrderId.of(orderId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!result.memberId().equals(MemberId.of(memberId))) {
            throw new AccessDeniedException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return toOrderDetailResponse(result, memberId);
    }

    private OrderListItemResponse toOrderListItemResponse(OrderListItemResult result) {
        return OrderListItemResponse.from(
            result.id(),
            result.shopName(),
            fileService.getUrlByPath(result.shopThumbnailImageFilePath()),
            result.firstProductName(),
            result.totalItemCount(),
            result.amount(),
            result.paymentStatus().name(),
            result.paymentDate()
        );
    }

    private OrderDetailResponse toOrderDetailResponse(OrderDetailResult result, Long memberId) {
        List<OrderProductResponse> orderProducts = result.orderProducts().stream()
            .map(orderProduct -> toOrderProductResponse(orderProduct, result.id(), memberId))
            .toList();

        PaymentSummaryResponse payment = result.payment() != null ? toPaymentSummaryResponse(result.payment()) : null;

        return OrderDetailResponse.from(
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

    private OrderProductResponse toOrderProductResponse(OrderProductResult result, Long orderId, Long memberId) {
        boolean reviewed = reviewQueryService.isReviewedByOrderAndProduct(orderId, result.productId(), memberId);
        List<OrderProductOptionResponse> options = result.options().stream()
            .map(this::toOrderProductOptionResponse)
            .toList();
        return OrderProductResponse.from(
            result.orderProductId(),
            result.productId(),
            result.name(),
            fileService.getUrlByPath(result.imageFilePath()),
            result.quantity(),
            result.originalPrice(),
            result.discountPrice(),
            result.totalOptionPrice(),
            result.totalPrice(),
            options,
            reviewed
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
