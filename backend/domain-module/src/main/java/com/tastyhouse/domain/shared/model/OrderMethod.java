package com.tastyhouse.domain.shared.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 방식 — 배달·포장·예약·테이블 오더.
 *
 * <p><b>어느 한 컨텍스트가 아니라 {@code shared}가 소유한다.</b> 과거에는 {@code shop.model}에
 * 있었으나 {@code Order}·{@code OrderSchedule}이 이를 필드로 보유해 order → shop.model 방향의
 * 경계 위반을 만들고 있었다. 그렇다고 {@code order.model}로 옮기면 방향만 뒤집힐 뿐이다 — 가게별
 * 주문유형 배정({@code ShopOrderMethod})·영업상태 판정·예약 슬롯 계산 등 <b>shop 컨텍스트 18개
 * 클래스</b>가 이 enum을 쓰므로, shop → order.model 위반이 그만큼 새로 생긴다.
 *
 * <p>이 enum은 실제로 order와 shop 양쪽의 어휘이며 어느 한쪽의 애그리거트 내부 구현이 아니다.
 * {@code ApprovalStatus} 선례대로 전 컨텍스트 공용인 {@code shared}에 두면 양방향이 모두 정상
 * 참조가 되어(경계 규칙이 {@code shared}를 전면 허용) 위반이 새로 생기지 않는다.
 *
 * <p><b>DB·API 계약은 바뀌지 않는다</b> — 상수명이 그대로이므로 {@code ORDER.order_method}·
 * {@code SHOP_ORDER_METHOD.order_method}에 저장되는 {@code VARCHAR} 값과 HTTP 경계에서 주고받는
 * 문자열이 이관 전과 동일하다. 이 이동은 순수 컴파일타임 재배치다.
 */
public enum OrderMethod {

    TABLE("테이블 오더"),
    RESERVATION("예약"),
    DELIVERY("배달"),
    TAKEOUT("포장");

    private final String displayName;

    OrderMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static OrderMethod from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.ORDER_METHOD_UNKNOWN,
                ErrorCode.ORDER_METHOD_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
