package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.application.reservation.port.out.ReservationResult;
import com.tastyhouse.application.reservation.port.out.ReservationCompleteDetailResult;
import com.tastyhouse.application.reservation.port.out.ReservationDetailViewResult;
import com.tastyhouse.application.reservation.port.out.ReservationSlotAvailabilityResult;

@WebApp
public interface ReservationQueryUseCase {

    ReservationSlotAvailabilityResult getAvailability(Long shopId, LocalDate date, Long memberId);

    List<ReservationResult> getMyReservations(Long memberId);

    List<ReservationResult> getShopReservations(Long shopId);

    ReservationResult getReservation(Long id);

    ReservationCompleteDetailResult getCompleteDetail(Long memberId, Long id);

    ReservationDetailViewResult getReservationDetail(Long memberId, Long id);
}
