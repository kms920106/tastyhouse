package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ScheduledOrderSlotItemResult;

@Schema(description = "예약 가능 수령시간 슬롯")
public record ScheduledOrderSlotItemResponse(
    @Schema(description = "슬롯 시작 시각. 주문 생성 시 이 값을 그대로 scheduledAt으로 보냅니다.", example = "2026-08-08T18:00:00")
    LocalDateTime startAt,

    @Schema(description = "슬롯 종료 시각. 포장(단일 시각)은 startAt과 동일합니다.", example = "2026-08-08T18:30:00")
    LocalDateTime endAt,

    @Schema(description = "표시용 문구. 배달은 범위, 포장은 단일 시각입니다.", example = "오후 6:00~오후 6:30")
    String label,

    @Schema(description = "날짜 구분 문구(오늘/내일). 자정 넘김 영업·24시간 가게에서 내일이 나올 수 있습니다.", example = "오늘")
    String dayLabel
) {
    public static ScheduledOrderSlotItemResponse from(ScheduledOrderSlotItemResult result) {
        return new ScheduledOrderSlotItemResponse(
            result.startAt(),
            result.endAt(),
            result.label(),
            result.dayLabel()
        );
    }
}
