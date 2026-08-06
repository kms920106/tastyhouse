package com.tastyhouse.domain.order.service;

import java.util.List;

import com.tastyhouse.domain.shop.model.OrderMethod;

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
 * @param deliveryAddressId 배달 주소 ID(입력). 주문 방법이 {@code DELIVERY}면 필수
 * @param deliveryTipAmount 클라이언트가 계산한 배달팁(대조용). 서버 계산값과 다르면 접수를 거절한다
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
    Integer finalAmount
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
        Integer finalAmount
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
            finalAmount
        );
    }
}
