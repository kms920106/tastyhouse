package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 예약 가능 수령시간 슬롯 조회 응답.
 *
 * <p><b>예약할 수 없는 상태도 404가 아니라 200 + {@code available:false}로 내려간다</b>(배달팁 통합 조회
 * 선례) — 예약주문 미운영·미지원 주문방식·영업 종료·영업시간 미등록은 모두 오류가 아니라 "지금은 예약할 수
 * 없다"는 정상적인 조회 결과이며, 프론트는 같은 분기 하나로 안내 문구를 띄운다.
 *
 * <p>{@code leadTimeMinutes}·{@code slotUnitMinutes}·{@code rangeSlot}은 안내 문구와 표시 형태를 위한
 * 값이다("2시간 이후부터 예약 가능"). 슬롯이 없어도 내려가므로 프론트가 상수를 복제하지 않는다.
 */
@Schema(description = "예약 가능 수령시간 슬롯 조회 응답")
public record ScheduledOrderSlotsResponse(
    @Schema(description = "예약 가능 여부. 예약주문 운영 중이고 슬롯이 1개 이상일 때만 true입니다.", example = "true")
    boolean available,

    @Schema(description = "리드타임(분). 배달 120, 포장 60. 안내 문구용입니다.", example = "120")
    int leadTimeMinutes,

    @Schema(description = "슬롯 단위(분). 30 고정입니다.", example = "30")
    int slotUnitMinutes,

    @Schema(description = "슬롯이 범위 표기인지 여부. DELIVERY는 true(18:00~18:30), TAKEOUT은 false(18:00)입니다.", example = "true")
    boolean rangeSlot,

    @Schema(description = "예약 가능 슬롯 목록. 시작 시각 오름차순이며, 예약할 수 없으면 빈 배열입니다.")
    List<ScheduledOrderSlotItemResponse> slots
) {

    public static ScheduledOrderSlotsResponse from(
        boolean available,
        int leadTimeMinutes,
        int slotUnitMinutes,
        boolean rangeSlot,
        List<ScheduledOrderSlotItemResponse> slots
    ) {
        return new ScheduledOrderSlotsResponse(
            available,
            leadTimeMinutes,
            slotUnitMinutes,
            rangeSlot,
            slots
        );
    }
}
