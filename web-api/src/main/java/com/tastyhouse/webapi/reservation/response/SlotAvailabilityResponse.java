package com.tastyhouse.webapi.reservation.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.reservation.application.dto.result.DailySlotAvailabilityResult;

@Schema(description = "날짜별 슬롯 가용성 응답")
public record SlotAvailabilityResponse(
    @Schema(description = "조회 날짜", example = "2026-06-10")
    LocalDate date,

    @Schema(description = "이 날짜에 로그인 회원의 차단 예약(PENDING/CONFIRMED/COMPLETED)이 있는지 여부. true면 모든 슬롯이 available=false", example = "false")
    boolean hasMyReservation,

    @Schema(description = "슬롯별 가용 정보")
    List<Slot> slots
) {
    public static SlotAvailabilityResponse from(DailySlotAvailabilityResult result) {
        List<Slot> slots = result.slots().stream()
            .map(s -> new Slot(s.time(), s.remaining(), s.available()))
            .toList();
        return new SlotAvailabilityResponse(result.date(), result.hasMyReservation(), slots);
    }
}
