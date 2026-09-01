package com.tastyhouse.webapi.reservation.adapter.in.web.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.reservation.port.out.ReservationSlotAvailabilityResult;

@Schema(description = "날짜별 슬롯 가용성 응답")
public record ReservationSlotAvailabilityResponse(
    @Schema(description = "조회 날짜", example = "2026-06-10")
    LocalDate date,

    @Schema(description = "이 날짜에 로그인 회원의 차단 예약(PENDING/CONFIRMED/COMPLETED)이 있는지 여부. true면 모든 슬롯이 available=false", example = "false")
    boolean hasMyReservation,

    @Schema(description = "슬롯별 가용 정보")
    List<ReservationSlot> slots
) {

    public static ReservationSlotAvailabilityResponse from(ReservationSlotAvailabilityResult result) {
        return new ReservationSlotAvailabilityResponse(
            result.date(),
            result.hasMyReservation(),
            result.slots().stream()
                .map(ReservationSlot::from)
                .toList()
        );
    }
}
