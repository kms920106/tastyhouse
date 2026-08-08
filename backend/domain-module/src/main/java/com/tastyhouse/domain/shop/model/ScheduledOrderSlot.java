package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

/**
 * 예약 가능한 수령 시간 슬롯 하나.
 *
 * <p>배달은 30분 <b>범위</b>({@code 18:00~18:30})이고 포장은 단일 <b>시각</b>({@code 18:00})이므로,
 * 포장 슬롯은 {@code endAt == startAt}으로 표현한다. 두 경우를 서로 다른 타입으로 나누지 않는 이유는
 * 계산기·주문 접수·응답 조립이 모두 "슬롯 시작 시각"만으로 동작하고, 종료 시각은 표시와 스냅샷에만
 * 쓰이기 때문이다.
 *
 * @param startAt 슬롯 시작 시각 — 주문 생성 시 클라이언트가 그대로 되보내는 값이다
 * @param endAt   슬롯 종료 시각. 포장은 {@code startAt}과 동일
 */
public record ScheduledOrderSlot(
    LocalDateTime startAt,
    LocalDateTime endAt
) {

    /** 배달 슬롯 — 시작 시각부터 30분 범위. */
    public static ScheduledOrderSlot range(LocalDateTime startAt) {
        return new ScheduledOrderSlot(startAt, startAt.plusMinutes(ScheduledOrderPolicy.SLOT_UNIT_MINUTES));
    }

    /** 포장 슬롯 — 단일 시각(종료 시각이 시작 시각과 같다). */
    public static ScheduledOrderSlot instant(LocalDateTime startAt) {
        return new ScheduledOrderSlot(startAt, startAt);
    }

    /**
     * 주문방식에 맞는 슬롯을 만든다 — 배달은 범위, 그 외(포장)는 단일 시각.
     */
    public static ScheduledOrderSlot of(OrderMethod orderMethod, LocalDateTime startAt) {
        return ScheduledOrderPolicy.isRangeSlot(orderMethod) ? range(startAt) : instant(startAt);
    }

    /**
     * 이 슬롯이 주어진 시각을 <b>시작 시각으로</b> 갖는지 판정한다.
     *
     * <p>범위 포함이 아니라 시작 시각 일치인 이유는, 클라이언트가 되보내는 값이 슬롯 목록에서 고른
     * {@code startAt} 그 자체이기 때문이다 — 30분 단위를 벗어난 임의 시각은 어느 슬롯과도 일치하지 않아
     * 자연히 거절된다.
     */
    public boolean matches(LocalDateTime scheduledAt) {
        return this.startAt.equals(scheduledAt);
    }
}
