package com.tastyhouse.application.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.application.reservation.port.in.ReservationDetailByIdQueryUseCase;
import com.tastyhouse.application.reservation.port.out.ReservationQueryPort;
import com.tastyhouse.application.reservation.port.out.ReservationResult;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class ReservationDetailByIdQueryService implements ReservationDetailByIdQueryUseCase {

    private final ReservationQueryPort reservationQueryPort;

    public ReservationDetailByIdQueryService(ReservationQueryPort reservationQueryPort) {
        this.reservationQueryPort = reservationQueryPort;
    }

    @Override
    public ReservationResult getReservation(Long id) {
        return reservationQueryPort.findReservationById(ReservationId.of(id).value())
            .orElseThrow(() -> new ApplicationException(WebErrorCode.RESERVATION_NOT_FOUND));
    }
}
