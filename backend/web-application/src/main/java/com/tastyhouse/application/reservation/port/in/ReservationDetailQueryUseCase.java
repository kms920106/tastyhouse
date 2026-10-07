package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.application.reservation.port.out.ReservationDetailViewResult;

public interface ReservationDetailQueryUseCase {

    ReservationDetailViewResult getReservationDetail(Long memberId, Long id);
}
