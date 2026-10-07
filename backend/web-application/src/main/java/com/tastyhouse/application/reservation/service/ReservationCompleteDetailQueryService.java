package com.tastyhouse.application.reservation.service;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.application.reservation.port.in.ReservationCompleteDetailQueryUseCase;
import com.tastyhouse.application.reservation.port.out.ReservationCompleteDetailResult;
import com.tastyhouse.application.reservation.port.out.ReservationQueryPort;
import com.tastyhouse.application.reservation.port.out.ReservationResult;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class ReservationCompleteDetailQueryService implements ReservationCompleteDetailQueryUseCase {

    private final ReservationQueryPort reservationQueryPort;

    public ReservationCompleteDetailQueryService(ReservationQueryPort reservationQueryPort) {
        this.reservationQueryPort = reservationQueryPort;
    }

    @Override
    public ReservationCompleteDetailResult getCompleteDetail(Long memberId, Long id) {
        ReservationResult result = reservationQueryPort.findReservationById(ReservationId.of(id).value())
            .orElseThrow(() -> new ApplicationException(WebErrorCode.RESERVATION_NOT_FOUND));
        validateOwnership(result.memberId(), memberId);

        return new ReservationCompleteDetailResult(
            result.id(),
            result.shopName(),
            result.shopImageUrl(),
            LocalDateTime.of(result.reservationDate(), result.reservationTime()),
            result.partySize()
        );
    }

    private void validateOwnership(Long ownerId, Long requesterId) {
        if (!Objects.equals(ownerId, requesterId)) {
            throw new DomainException(DomainErrorCode.RESERVATION_ACCESS_DENIED);
        }
    }
}
