package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.application.reservation.port.out.ReservationCompleteDetailResult;

public interface ReservationCompleteDetailQueryUseCase {

    ReservationCompleteDetailResult getCompleteDetail(Long memberId, Long id);
}
