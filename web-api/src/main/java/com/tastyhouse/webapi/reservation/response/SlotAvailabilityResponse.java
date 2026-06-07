package com.tastyhouse.webapi.reservation.response;

import com.tastyhouse.core.domain.reservation.application.dto.result.DailySlotAvailabilityResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "날짜별 슬롯 가용성 응답")
public record SlotAvailabilityResponse(
    @Schema(description = "조회 날짜", example = "2026-06-10")
    LocalDate date,

    @Schema(description = "이 날짜에 로그인 회원의 차단 예약(PENDING/CONFIRMED/COMPLETED)이 있는지 여부. true면 모든 슬롯이 available=false", example = "false")
    boolean hasMyReservation,

    @Schema(description = "슬롯별 가용 정보")
    List<Slot> slots
) {
    @Schema(description = "슬롯 가용 정보")
    public record Slot(
        @Schema(description = "슬롯 시간", example = "13:00")
        LocalTime time,

        @Schema(description = "잔여 예약 가능 수", example = "7")
        int remaining,

        @Schema(description = "예약 가능 여부 (잔여>0 && 미과거)", example = "true")
        boolean available
    ) {
    }

    public static SlotAvailabilityResponse from(DailySlotAvailabilityResult result) {
        List<Slot> slots = result.slots().stream()
            .map(s -> new Slot(s.time(), s.remaining(), s.available()))
            .toList();
        return new SlotAvailabilityResponse(result.date(), result.hasMyReservation(), slots);
    }
}
