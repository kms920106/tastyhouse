package com.tastyhouse.application.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.reservation.port.in.ReservationCreateCommand;
import com.tastyhouse.application.reservation.port.in.ReservationCreateUseCase;
import com.tastyhouse.application.shared.port.out.OptimisticLockConflictException;
import com.tastyhouse.application.shared.port.out.UniqueConstraintConflictException;

@Service
class ReservationCreateService implements ReservationCreateUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReservationCreateService.class);

    private static final int MAX_RETRY = 3;

    private final ReservationBookingExecutor reservationBookingExecutor;

    public ReservationCreateService(ReservationBookingExecutor reservationBookingExecutor) {
        this.reservationBookingExecutor = reservationBookingExecutor;
    }

    @Override
    public Long createReservation(ReservationCreateCommand command) {
        Long shopId = command.shopId();
        LocalDate reservationDate = command.reservationDate();
        LocalTime reservationTime = command.reservationTime();
        MemberId memberIdVo = MemberId.of(command.memberId());
        ShopId shopIdVo = ShopId.of(shopId);

        for (int attempt = 0; ; attempt++) {
            try {
                ReservationId reservationId = reservationBookingExecutor.bookInNewTx(
                    memberIdVo,
                    shopIdVo,
                    reservationDate,
                    reservationTime,
                    command.partySize(),
                    command.request(),
                    command.agreedRequiredTerms()
                );
                return reservationId.value();
            } catch (OptimisticLockConflictException | UniqueConstraintConflictException e) {
                log.warn("예약 생성 동시성 경합 재시도 {}/{}: shopId={}, date={}, time={}",
                    attempt + 1, MAX_RETRY, shopId, reservationDate, reservationTime);
                if (attempt == MAX_RETRY - 1) {

                    throw new DomainException(DomainErrorCode.RESERVATION_SLOT_FULL);
                }
            }
        }
    }
}
