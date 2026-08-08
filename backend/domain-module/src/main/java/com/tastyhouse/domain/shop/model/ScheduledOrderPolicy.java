package com.tastyhouse.domain.shop.model;

/**
 * 예약주문 공용 정책 상수({@link DeliveryTipPolicy} 선례의 상수 전용 final class).
 *
 * <p>리드타임·슬롯 단위는 <b>점주 설정으로 열지 않고 도메인 상수로 고정</b>한다. 배민 셀프서비스 가이드가
 * 규정한 값이며, 점주 설정으로 열면 슬롯 계산 검증과 설정 UI가 함께 복잡해지는 데 비해 얻는 것이 없다.
 *
 * <p>대상 주문방식은 {@link OrderMethod#DELIVERY}·{@link OrderMethod#TAKEOUT} 둘뿐이다 —
 * {@code TABLE}·{@code RESERVATION}은 "음식을 받는 시각"이라는 개념 자체가 없다.
 */
public final class ScheduledOrderPolicy {

    /** 배달 예약 리드타임(분) — 영업 시작 시각 기준 2시간 이후부터 예약 가능. */
    public static final int DELIVERY_LEAD_TIME_MINUTES = 120;

    /** 포장 예약 리드타임(분) — 영업 시작 시각 기준 1시간 이후부터 예약 가능. */
    public static final int TAKEOUT_LEAD_TIME_MINUTES = 60;

    /** 슬롯 단위(분). 배달은 이 길이의 범위, 포장은 이 간격의 단일 시각이다. */
    public static final int SLOT_UNIT_MINUTES = 30;

    /** 24시간 운영 가게의 예약 가능 상한(시간) — 당일 제한 대신 주문 시각 +24시간까지 허용한다. */
    public static final int MAX_HORIZON_HOURS_FOR_24H_SHOP = 24;

    private ScheduledOrderPolicy() {
    }

    /**
     * 이 주문방식이 예약주문을 지원하는지 판정한다.
     */
    public static boolean supports(OrderMethod orderMethod) {
        return orderMethod == OrderMethod.DELIVERY || orderMethod == OrderMethod.TAKEOUT;
    }

    /**
     * 주문방식별 리드타임(분). 지원하지 않는 주문방식은 호출 전에 {@link #supports(OrderMethod)}로 걸러야 한다.
     *
     * @throws IllegalArgumentException 예약주문을 지원하지 않는 주문방식인 경우
     */
    public static int leadTimeMinutes(OrderMethod orderMethod) {
        return switch (orderMethod) {
            case DELIVERY -> DELIVERY_LEAD_TIME_MINUTES;
            case TAKEOUT -> TAKEOUT_LEAD_TIME_MINUTES;
            default -> throw new IllegalArgumentException("예약주문을 지원하지 않는 주문 방법입니다: " + orderMethod);
        };
    }

    /**
     * 슬롯이 범위({@code 18:00~18:30})인지, 단일 시각({@code 18:00})인지 — 배달만 범위다.
     *
     * <p>표시 계층이 이 값으로 문구를 고른다(PDF 규격: 배달은 범위 표기, 포장은 시각 표기).
     */
    public static boolean isRangeSlot(OrderMethod orderMethod) {
        return orderMethod == OrderMethod.DELIVERY;
    }
}
