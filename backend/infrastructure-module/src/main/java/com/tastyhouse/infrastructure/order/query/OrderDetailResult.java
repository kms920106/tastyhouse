package com.tastyhouse.infrastructure.order.query;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;

/**
 * 주문 단건 상세 조회 결과 — 주문 헤더에 가게명·가게 전화번호를 join하고, 상품 라인 목록과 결제 요약을
 * 함께 담는다. web-api(내 주문 상세)·admin-api(주문 관리 상세)가 같은 필드 셋을 쓰므로 하나로 둔다.
 *
 * <p>1:N인 상품 라인과 0..1인 결제는 한 번의 투영으로 채울 수 없어, {@code @QueryProjection}은 그 둘을
 * 제외한 좁은 생성자에 붙이고 DAO가 별도 조회한 값을 {@link #withOrderProducts}/{@link #withPayment}로
 * 덧붙인다(review 도메인 {@code ReviewDetailResult} 선례와 동일한 관용구).
 *
 * <p>{@code memberId}는 소유권 검증이 아니라 화면 표기·감사 목적의 부가 정보다 — 소유권 검증 자체는
 * write 경로의 {@code OrderTransitionService#loadOwnedBy}(도메인 모델 {@code Order.validateOwnership})가
 * 담당한다.
 */
public record OrderDetailResult(
    Long id,
    Long memberId,
    String orderNumber,
    OrderMethod orderMethod,
    OrderStatus orderStatus,
    String shopName,
    String shopPhoneNumber,
    String ordererName,
    String ordererPhone,
    String ordererEmail,
    Integer totalProductAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer pointDiscountAmount,
    Integer totalDiscountAmount,
    Integer finalAmount,
    Integer usedPoint,
    Integer earnedPoint,
    LocalDateTime createdAt,

    // 수령 예약 시각(슬롯 시작). null이면 즉시 주문.
    LocalDateTime scheduledAt,

    // 수령 예약 슬롯 종료 시각. 포장은 scheduledAt과 동일.
    LocalDateTime scheduledSlotEndAt,

    List<OrderProductResult> orderProducts,
    OrderPaymentResult payment
) {
    /**
     * QueryDSL 투영 전용 생성자 — 1:N인 상품 라인과 0..1인 결제를 제외한 좁은 시그니처다. 호출부는 생성된
     * {@code QOrderDetailResult}({@code OrderQueryDao#findOrderDetail})이므로 IDE가 "never used"로
     * 경고하지만, 제거하면 Q타입이 생성되지 않아 빌드가 깨진다.
     */
    @QueryProjection
    public OrderDetailResult(
        Long id,
        Long memberId,
        String orderNumber,
        OrderMethod orderMethod,
        OrderStatus orderStatus,
        String shopName,
        String shopPhoneNumber,
        String ordererName,
        String ordererPhone,
        String ordererEmail,
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        Integer usedPoint,
        Integer earnedPoint,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime scheduledSlotEndAt
    ) {
        this(
            id,
            memberId,
            orderNumber,
            orderMethod,
            orderStatus,
            shopName,
            shopPhoneNumber,
            ordererName,
            ordererPhone,
            ordererEmail,
            totalProductAmount,
            productDiscountAmount,
            couponDiscountAmount,
            pointDiscountAmount,
            totalDiscountAmount,
            finalAmount,
            usedPoint,
            earnedPoint,
            createdAt,
            scheduledAt,
            scheduledSlotEndAt,
            List.of(),
            null
        );
    }

    public OrderDetailResult withOrderProducts(List<OrderProductResult> orderProducts) {
        return new OrderDetailResult(
            id,
            memberId,
            orderNumber,
            orderMethod,
            orderStatus,
            shopName,
            shopPhoneNumber,
            ordererName,
            ordererPhone,
            ordererEmail,
            totalProductAmount,
            productDiscountAmount,
            couponDiscountAmount,
            pointDiscountAmount,
            totalDiscountAmount,
            finalAmount,
            usedPoint,
            earnedPoint,
            createdAt,
            scheduledAt,
            scheduledSlotEndAt,
            orderProducts,
            payment
        );
    }

    public OrderDetailResult withPayment(OrderPaymentResult payment) {
        return new OrderDetailResult(
            id,
            memberId,
            orderNumber,
            orderMethod,
            orderStatus,
            shopName,
            shopPhoneNumber,
            ordererName,
            ordererPhone,
            ordererEmail,
            totalProductAmount,
            productDiscountAmount,
            couponDiscountAmount,
            pointDiscountAmount,
            totalDiscountAmount,
            finalAmount,
            usedPoint,
            earnedPoint,
            createdAt,
            scheduledAt,
            scheduledSlotEndAt,
            orderProducts,
            payment
        );
    }
}
