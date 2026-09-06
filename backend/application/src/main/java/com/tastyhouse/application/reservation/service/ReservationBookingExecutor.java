package com.tastyhouse.application.reservation.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.service.ReservationBookingService;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Component
@WebApp
public class ReservationBookingExecutor {

    private final ReservationBookingService reservationBookingService;

    public ReservationBookingExecutor(ReservationBookingService reservationBookingService) {
        this.reservationBookingService = reservationBookingService;
    }

    @Transactional
    public ReservationId bookInNewTx(
        MemberId memberId,
        ShopId shopId,
        LocalDate date,
        LocalTime time,
        Integer partySize,
        String request,
        boolean agreedRequiredTerms
    ) {
        return reservationBookingService.book(
            memberId,
            shopId,
            date,
            time,
            partySize,
            request,
            agreedRequiredTerms
        );
    }
}
