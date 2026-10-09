package com.tastyhouse.application.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.application.reservation.port.in.ReservationCompleteCommand;
import com.tastyhouse.application.reservation.port.in.ReservationCompleteUseCase;
import com.tastyhouse.application.reservation.port.out.write.ReservationLoadPort;
import com.tastyhouse.application.reservation.port.out.write.ReservationSavePort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional
class ReservationCompleteService implements ReservationCompleteUseCase {

    private final ReservationLoadPort reservationLoadPort;
    private final ReservationSavePort reservationSavePort;

    public ReservationCompleteService(ReservationLoadPort reservationLoadPort, ReservationSavePort reservationSavePort) {
        this.reservationLoadPort = reservationLoadPort;
        this.reservationSavePort = reservationSavePort;
    }

    @Override
    public void completeReservation(ReservationCompleteCommand command) {
        ReservationId reservationId = ReservationId.of(command.reservationId());
        Reservation reservation = getReservation(reservationId);
        reservation.complete();
        reservationSavePort.save(reservation);
    }

    private Reservation getReservation(ReservationId reservationId) {
        return reservationLoadPort.findById(reservationId)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.RESERVATION_NOT_FOUND));
    }
}
