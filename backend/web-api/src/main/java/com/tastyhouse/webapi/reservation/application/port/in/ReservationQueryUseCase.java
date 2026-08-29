package com.tastyhouse.webapi.reservation.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.webapi.reservation.adapter.in.web.response.ReservationCompleteDetailResponse;
import com.tastyhouse.webapi.reservation.adapter.in.web.response.ReservationDetailResponse;
import com.tastyhouse.webapi.reservation.adapter.in.web.response.ReservationResponse;
import com.tastyhouse.webapi.reservation.adapter.in.web.response.ReservationSlotAvailabilityResponse;

/**
 * 예약 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReservationQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ReservationQueryUseCase {

    ReservationSlotAvailabilityResponse getAvailability(Long shopId, LocalDate date, Long memberId);

    List<ReservationResponse> getMyReservations(Long memberId);

    List<ReservationResponse> getShopReservations(Long shopId);

    ReservationResponse getReservation(Long id);

    ReservationCompleteDetailResponse getCompleteDetail(Long memberId, Long id);

    ReservationDetailResponse getReservationDetail(Long memberId, Long id);
}
