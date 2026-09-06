package com.tastyhouse.domain.order.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;

/**
 * 주문 접수 입력 — 한 건의 주문에 담기는 상품 라인 목록과 결제 수단(쿠폰·포인트), 그리고 클라이언트가
 * 계산해 보내온 금액 검증값을 함께 나른다.
 *
 * <p>소비 모듈의 command 서비스가 HTTP 요청을 이 형태로 조립해
 * {@link OrderPlacementService#place}에 넘긴다. 도메인 계층 타입이므로 프레임워크 어노테이션을 갖지
 * 않으며, 검증(금액 대조)은 도메인 서비스가 수행한다. {@code orderMethod}는 도메인 enum이므로 HTTP
 * 경계의 {@code String}을 소비 모듈 command 서비스에서 {@code OrderMethod.from(...)}으로 승격해 담는다.
 *
 * <p><b>배달 주소는 좌표가 아니라 {@code deliveryAddressId}만 받는다.</b> 좌표를 요청 본문으로 받으면
 * 가짜 좌표를 보내 거리별 배달팁을 0원까지 낮출 수 있다 — 서버는 <b>저장된 주소에서만</b> 좌표를 읽는다.
 *
 * <p><b>수령 예약시간({@code scheduledAt})도 클라이언트 값을 그대로 신뢰하지 않는다</b> — 서버가 유효
 * 슬롯을 재계산해 대조하고, 일치하는 슬롯이 없으면 접수를 거절한다(배달팁 금액 대조와 같은 원칙).
 *
 * @param deliveryAddressId 배달 주소 ID(입력). 주문 방법이 {@code DELIVERY}면 필수
 * @param cupDepositAmount 클라이언트가 계산한 일회용컵 보증금(대조용). 서버가 옵션의 컵 개수 × 요율로
 *     계산한 값과 다르면 접수를 거절한다. {@code null}은 0으로 본다 — 보증금 필드를 아직 보내지 않는
 *     클라이언트는 보증금 옵션을 고르지 않은 주문만 통과하므로 프론트 배포 순서와 독립이다.
 * @param deliveryTipAmount 클라이언트가 계산한 배달팁(대조용). 서버 계산값과 다르면 접수를 거절한다
 * @param scheduledAt       수령 예약 시각(슬롯 시작). {@code null}이면 즉시 주문
 */
public record OrderPlacement(
    Long shopId,
    OrderMethod orderMethod,
    List<OrderPlacementItem> items,
    Long memberCouponId,
    Integer usePoint,
    Long deliveryAddressId,
    Integer totalProductAmount,
    Integer totalDiscountAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer deliveryTipAmount,
    Integer cupDepositAmount,
    Integer finalAmount,
    LocalDateTime scheduledAt
) {

    public static OrderPlacement of(
        Long shopId,
        OrderMethod orderMethod,
        List<OrderPlacementItem> items,
        Long memberCouponId,
        Integer usePoint,
        Long deliveryAddressId,
        Integer totalProductAmount,
        Integer totalDiscountAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer deliveryTipAmount,
        Integer cupDepositAmount,
        Integer finalAmount,
        LocalDateTime scheduledAt
    ) {
        return new OrderPlacement(
            shopId,
            orderMethod,
            items,
            memberCouponId,
            usePoint,
            deliveryAddressId,
            totalProductAmount,
            totalDiscountAmount,
            productDiscountAmount,
            couponDiscountAmount,
            deliveryTipAmount,
            cupDepositAmount,
            finalAmount,
            scheduledAt
        );
    }
}
