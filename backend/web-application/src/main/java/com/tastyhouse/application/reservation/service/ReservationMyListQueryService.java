package com.tastyhouse.application.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.reservation.port.in.ReservationMyListQueryUseCase;
import com.tastyhouse.application.reservation.port.out.ReservationQueryPort;
import com.tastyhouse.application.reservation.port.out.ReservationResult;

@Service
@Transactional(readOnly = true)
class ReservationMyListQueryService implements ReservationMyListQueryUseCase {

    private final ReservationQueryPort reservationQueryPort;

    public ReservationMyListQueryService(ReservationQueryPort reservationQueryPort) {
        this.reservationQueryPort = reservationQueryPort;
    }

    @Override
    public List<ReservationResult> getMyReservations(Long memberId) {
        return reservationQueryPort.findReservationsByMemberId(memberId);
    }
}
