package com.tastyhouse.webapi.order;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.order.query.OrderDetailResult;
import com.tastyhouse.infrastructure.order.query.OrderListItemResult;
import com.tastyhouse.infrastructure.order.query.OrderPaymentResult;
import com.tastyhouse.infrastructure.order.query.OrderProductOptionResult;
import com.tastyhouse.infrastructure.order.query.OrderProductResult;
import com.tastyhouse.infrastructure.order.query.OrderQueryDao;
import com.tastyhouse.apicommon.common.PaginationResponse;
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
public class OrderQueryService {

    private final OrderQueryDao orderQueryDao;
    private final ReviewQueryService reviewQueryService;

    public OrderQueryService(OrderQueryDao orderQueryDao, ReviewQueryService reviewQueryService) {
        this.orderQueryDao = orderQueryDao;
        this.reviewQueryService = reviewQueryService;
    }

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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (!result.memberId().equals(MemberId.of(memberId))) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return toOrderDetailResponse(result, memberId);
    }

    private OrderListItemResponse toOrderListItemResponse(OrderListItemResult result) {
        return OrderListItemResponse.from(
            result.id(),
            result.shopName(),
            result.shopThumbnailImageUrl(),
            result.firstProductName(),
            result.totalItemCount(),
            result.amount(),
            result.paymentStatus().name(),
            result.paymentDate()
        );
    }

    private OrderDetailResponse toOrderDetailResponse(OrderDetailResult result, Long memberId) {
        // 주문상품마다 리뷰 여부를 개별 조회하면 상품 수만큼 쿼리가 나가므로(N+1), 상품 식별자를 모아
        // 한 번에 조회하고 아래 매핑 루프는 메모리에서 판정한다.
        List<Long> productIds = result.orderProducts().stream()
            .map(OrderProductResult::productId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Set<Long> reviewedProductIds = reviewQueryService.findReviewedProductIds(result.id(), memberId, productIds);

        List<OrderProductResponse> orderProducts = result.orderProducts().stream()
            .map(orderProduct -> toOrderProductResponse(orderProduct, reviewedProductIds))
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

    private OrderProductResponse toOrderProductResponse(OrderProductResult result, Set<Long> reviewedProductIds) {
        boolean reviewed = reviewedProductIds.contains(result.productId());
        List<OrderProductOptionResponse> options = result.options().stream()
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
