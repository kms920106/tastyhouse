package com.tastyhouse.application.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.application.reservation.port.in.ReservationCancelCommand;
import com.tastyhouse.application.reservation.port.in.ReservationCancelUseCase;

@Service
@Transactional
class ReservationCancelService implements ReservationCancelUseCase {

    private final ReservationBookingService reservationBookingService;

    public ReservationCancelService(ReservationBookingService reservationBookingService) {
        this.reservationBookingService = reservationBookingService;
    }

    @Override
    public void cancelReservation(ReservationCancelCommand command) {
        ReservationId reservationId = ReservationId.of(command.reservationId());
        MemberId memberIdVo = MemberId.of(command.memberId());
        reservationBookingService.cancel(reservationId, memberIdVo);
    }
}
