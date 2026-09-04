package com.tastyhouse.application.shop.port.out;

import java.util.List;

/**
 * 예약 가능 수령시간 슬롯 조회 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 예약할 수 없는 상태도 오류가 아니라 {@code available:false} + 빈 목록으로
 * 표현하는데, 그 판정과 함께 내려보내는 리드타임·슬롯 단위·범위 여부가 모두 도메인 정책
 * ({@code ScheduledOrderPolicy})에서 읽는 값이다 — 미지원 주문방식이면 리드타임을 물을 수 없어 0으로
 * 내리는 분기도 여기 포함된다. 그 전부가 서비스에 남고 결과만 담긴다.
 */
public record ScheduledOrderSlotsViewResult(
    boolean available,
    int leadTimeMinutes,
    int slotUnitMinutes,
    boolean rangeSlot,
    List<ScheduledOrderSlotItemResult> slots
) {
}
