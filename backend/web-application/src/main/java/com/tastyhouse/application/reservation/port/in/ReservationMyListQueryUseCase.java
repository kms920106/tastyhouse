package com.tastyhouse.application.reservation.port.in;

import java.util.List;

import com.tastyhouse.application.reservation.port.out.ReservationResult;

public interface ReservationMyListQueryUseCase {

    List<ReservationResult> getMyReservations(Long memberId);
}
