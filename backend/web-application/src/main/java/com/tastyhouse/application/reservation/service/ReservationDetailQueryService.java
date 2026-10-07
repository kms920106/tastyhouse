package com.tastyhouse.application.reservation.service;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.application.reservation.port.in.ReservationDetailQueryUseCase;
import com.tastyhouse.application.reservation.port.out.ReservationDetailResult;
import com.tastyhouse.application.reservation.port.out.ReservationDetailViewResult;
import com.tastyhouse.application.reservation.port.out.ReservationQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional(readOnly = true)
class ReservationDetailQueryService implements ReservationDetailQueryUseCase {

    private final ReservationQueryPort reservationQueryPort;

    public ReservationDetailQueryService(ReservationQueryPort reservationQueryPort) {
        this.reservationQueryPort = reservationQueryPort;
    }

    @Override
    public ReservationDetailViewResult getReservationDetail(Long memberId, Long id) {
        ReservationDetailResult result = reservationQueryPort.findReservationDetailById(ReservationId.of(id).value())
            .orElseThrow(() -> new ApplicationException(WebErrorCode.RESERVATION_NOT_FOUND));
        validateOwnership(result.memberId(), memberId);

        return new ReservationDetailViewResult(
            result.id(),
            result.shopId(),
            result.shopName(),
            result.shopImageUrl(),
            result.shopRoadAddress(),
            result.shopLotAddress(),
            result.memberId(),
            result.reserverName(),
            result.reserverPhoneNumber(),
            result.reserverEmail(),
            LocalDateTime.of(result.reservationDate(), result.reservationTime()),
            result.partySize(),
            result.status(),
            result.request(),
            result.createdAt()
        );
    }

    private void validateOwnership(Long ownerId, Long requesterId) {
        if (!Objects.equals(ownerId, requesterId)) {
            throw new DomainException(DomainErrorCode.RESERVATION_ACCESS_DENIED);
        }
    }
}
