package com.tastyhouse.webapi.reservation;

import com.tastyhouse.core.domain.reservation.application.ReservationQueryService;
import com.tastyhouse.core.domain.reservation.application.dto.result.DailySlotAvailabilityResult;
import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.reservation.response.SlotAvailabilityResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.service.CustomUserDetails;
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
 * 슬롯 가용성 조회 (로그인 필수).
 * 로그인한 회원의 기존 예약 시간을 반영해 해당 슬롯을 비활성(available=false)으로 내려준다.
 * 미인증 시 401 반환.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "예약 API")
public class ShopReservationAvailabilityController {

    private final ReservationQueryService reservationQueryService;

    @Operation(summary = "슬롯 가용성 조회", description = "가게의 특정 날짜 슬롯별 잔여/가용 정보를 조회합니다. 로그인 필수 — 내 예약 슬롯은 available=false로 반환.")
    @GetMapping("/api/shops/{shopId}/reservations/availability")
    public ResponseEntity<ApiResponse<SlotAvailabilityResponse>> getAvailability(
        @PathVariable Long shopId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @CurrentUser CustomUserDetails userDetails
    ) {
        DailySlotAvailabilityResult result = reservationQueryService.findSlotAvailability(shopId, date, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(SlotAvailabilityResponse.from(result)));
    }
}
