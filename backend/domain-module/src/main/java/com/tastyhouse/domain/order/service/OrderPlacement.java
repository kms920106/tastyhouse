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
 */
public record OrderPlacement(
    Long shopId,
    OrderMethod orderMethod,
    List<OrderPlacementItem> items,
    Long memberCouponId,
    Integer usePoint,
    Integer totalProductAmount,
    Integer totalDiscountAmount,
    Integer productDiscountAmount,
    Integer couponDiscountAmount,
    Integer finalAmount
) {

    public static OrderPlacement of(
        Long shopId,
        OrderMethod orderMethod,
        List<OrderPlacementItem> items,
        Long memberCouponId,
        Integer usePoint,
        Integer totalProductAmount,
        Integer totalDiscountAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer finalAmount
    ) {
        return new OrderPlacement(
            shopId,
            orderMethod,
            items,
            memberCouponId,
            usePoint,
            totalProductAmount,
            totalDiscountAmount,
            productDiscountAmount,
            couponDiscountAmount,
            finalAmount
        );
    }
}
