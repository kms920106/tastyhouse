package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.model.OrderMethod;

/**
 * 가게에 배정된 주문방식 한 건(회원 상세·관리 화면 공용).
 *
 * <p>회원 화면은 주문방식 코드·표시명만 쓰고 관리 화면은 배정 식별자도 함께 쓰므로, 두 소비자의
 * 필드 합이 {@code id} 하나 차이라 하나의 Result로 둔다. 표시명은 {@link OrderMethod}가 소유하므로
 * 여기서는 enum 자체만 투영하고 소비 측이 {@code getDisplayName()}으로 꺼낸다.
 */
public record ShopOrderMethodResult(
    Long id,
    OrderMethod orderMethod
) {
}
