package com.tastyhouse.application.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.application.reservation.port.in.ReservationRejectCommand;
import com.tastyhouse.application.reservation.port.in.ReservationRejectUseCase;

@Service
@Transactional
class ReservationRejectService implements ReservationRejectUseCase {

    private final ReservationBookingService reservationBookingService;

    public ReservationRejectService(ReservationBookingService reservationBookingService) {
        this.reservationBookingService = reservationBookingService;
    }

    @Override
    public void rejectReservation(ReservationRejectCommand command) {
        ReservationId reservationId = ReservationId.of(command.reservationId());
        reservationBookingService.reject(reservationId);
    }
}
