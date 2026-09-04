package com.tastyhouse.webapi.reservation.adapter.in.web.response;

import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.reservation.port.out.ReservationSlotResult;

@Schema(description = "슬롯 가용 정보")
public record ReservationSlot(
    @Schema(description = "슬롯 시간", example = "13:00")
    LocalTime time,

    @Schema(description = "잔여 예약 가능 수", example = "7")
    int remaining,

    @Schema(description = "예약 가능 여부 (잔여>0 && 미과거)", example = "true")
    boolean available
) {

    public static ReservationSlot from(ReservationSlotResult result) {
        return new ReservationSlot(
            result.time(),
            result.remaining(),
            result.available()
        );
    }
}
