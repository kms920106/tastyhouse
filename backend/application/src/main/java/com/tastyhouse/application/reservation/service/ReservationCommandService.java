package com.tastyhouse.application.reservation.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.repository.ReservationRepository;
import com.tastyhouse.domain.reservation.service.ReservationBookingService;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.exception.OptimisticLockConflictException;
import com.tastyhouse.application.reservation.port.in.ReservationCancelCommand;
import com.tastyhouse.application.reservation.port.in.ReservationCommandUseCase;
import com.tastyhouse.application.reservation.port.in.ReservationCompleteCommand;
import com.tastyhouse.application.reservation.port.in.ReservationConfirmCommand;
import com.tastyhouse.application.reservation.port.in.ReservationCreateCommand;
import com.tastyhouse.application.reservation.port.in.ReservationRejectCommand;

@Service
@WebApp
public class ReservationCommandService implements ReservationCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReservationCommandService.class);

    private static final int MAX_RETRY = 3;

    private final ReservationBookingExecutor reservationBookingExecutor;
    private final ReservationBookingService reservationBookingService;
    private final ReservationRepository reservationRepository;

    public ReservationCommandService(
        ReservationBookingExecutor reservationBookingExecutor,
        ReservationBookingService reservationBookingService,
        ReservationRepository reservationRepository
    ) {
        this.reservationBookingExecutor = reservationBookingExecutor;
        this.reservationBookingService = reservationBookingService;
        this.reservationRepository = reservationRepository;
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
            } catch (OptimisticLockConflictException | DataIntegrityViolationException e) {
                log.warn("예약 생성 동시성 경합 재시도 {}/{}: shopId={}, date={}, time={}",
                    attempt + 1, MAX_RETRY, shopId, reservationDate, reservationTime);
                if (attempt == MAX_RETRY - 1) {

                    throw new BusinessException(ErrorCode.RESERVATION_SLOT_FULL);
                }
            }
        }
    }

    @Transactional
    @Override
    public void confirmReservation(ReservationConfirmCommand command) {
        ReservationId reservationId = ReservationId.of(command.reservationId());
        Reservation reservation = getReservation(reservationId);
        reservation.confirm();
        reservationRepository.save(reservation);
    }

    @Transactional
    @Override
    public void completeReservation(ReservationCompleteCommand command) {
        ReservationId reservationId = ReservationId.of(command.reservationId());
        Reservation reservation = getReservation(reservationId);
        reservation.complete();
        reservationRepository.save(reservation);
    }

    @Transactional
    @Override
    public void rejectReservation(ReservationRejectCommand command) {
        ReservationId reservationId = ReservationId.of(command.reservationId());
        reservationBookingService.reject(reservationId);
    }

    @Transactional
    @Override
    public void cancelReservation(ReservationCancelCommand command) {
        ReservationId reservationId = ReservationId.of(command.reservationId());
        MemberId memberIdVo = MemberId.of(command.memberId());
        reservationBookingService.cancel(reservationId, memberIdVo);
    }

    private Reservation getReservation(ReservationId reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }
}
