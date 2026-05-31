package com.tastyhouse.webapi.reservation;

import com.tastyhouse.core.domain.reservation.application.ReservationQueryService;
import com.tastyhouse.core.domain.reservation.application.dto.result.DailySlotAvailabilityResult;
import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.reservation.response.SlotAvailabilityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 슬롯 가용성 조회 (공개 경로).
 * {@code /api/shops/**} 가 PublicPaths 공개라서 로그인 전에도 캘린더/슬롯 노출이 가능하다.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "예약 API")
public class ShopReservationAvailabilityController {

    private final ReservationQueryService reservationQueryService;

    @Operation(summary = "슬롯 가용성 조회", description = "가게의 특정 날짜 슬롯별 잔여/가용 정보를 조회합니다. (공개)")
    @GetMapping("/api/shops/{shopId}/reservations/availability")
    public ResponseEntity<ApiResponse<SlotAvailabilityResponse>> getAvailability(
        @PathVariable Long shopId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailySlotAvailabilityResult result = reservationQueryService.findSlotAvailability(shopId, date);
        return ResponseEntity.ok(ApiResponse.success(SlotAvailabilityResponse.from(result)));
    }
}
