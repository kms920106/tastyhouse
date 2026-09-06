package com.tastyhouse.application.reservation.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.reservation.service.SlotPolicy;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.application.reservation.port.out.ReservationDetailResult;
import com.tastyhouse.application.reservation.port.out.ReservationQueryPort;
import com.tastyhouse.application.reservation.port.out.ReservationResult;
import com.tastyhouse.application.reservation.port.out.SlotOccupancyResult;
import com.tastyhouse.application.reservation.port.in.ReservationQueryUseCase;
import com.tastyhouse.application.reservation.port.out.ReservationCompleteDetailResult;
import com.tastyhouse.application.reservation.port.out.ReservationDetailViewResult;
import com.tastyhouse.application.reservation.port.out.ReservationSlotAvailabilityResult;
import com.tastyhouse.application.reservation.port.out.ReservationSlotResult;

@Service
@WebApp
@Transactional(readOnly = true)
public class ReservationQueryService implements ReservationQueryUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationQueryPort reservationQueryPort;

    public ReservationQueryService(ReservationQueryPort reservationQueryPort) {
        this.reservationQueryPort = reservationQueryPort;
    }

    @Override
    public ReservationSlotAvailabilityResult getAvailability(Long shopId, LocalDate date, Long memberId) {
        Map<LocalTime, Integer> remainingByTime = reservationQueryPort.findSlotOccupancies(shopId, date).stream()
            .collect(Collectors.toMap(SlotOccupancyResult::slotTime, SlotOccupancyResult::remaining));

        boolean hasMyReservation = reservationQueryPort.existsBlockingReservation(memberId, shopId, date);

        LocalDateTime now = LocalDateTime.now(KST);

        List<ReservationSlotResult> slots = SlotPolicy.allSlots().stream()
            .map(time -> {
                int remaining = remainingByTime.getOrDefault(time, SlotPolicy.CAPACITY_PER_SLOT);
                boolean notPast = LocalDateTime.of(date, time).isAfter(now);

                boolean available = remaining > 0 && notPast && !hasMyReservation;
                return new ReservationSlotResult(time, remaining, available);
            })
            .toList();

        return new ReservationSlotAvailabilityResult(date, hasMyReservation, slots);
    }

    @Override
    public List<ReservationResult> getMyReservations(Long memberId) {
        return reservationQueryPort.findReservationsByMemberId(memberId);
    }

    @Override
    public List<ReservationResult> getShopReservations(Long shopId) {
        return reservationQueryPort.findReservationsByShopId(shopId);
    }

    @Override
    public ReservationResult getReservation(Long id) {
        return reservationQueryPort.findReservationById(ReservationId.of(id))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Override
    public ReservationCompleteDetailResult getCompleteDetail(Long memberId, Long id) {
        ReservationResult result = reservationQueryPort.findReservationById(ReservationId.of(id))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        validateOwnership(result.memberId(), memberId);

        return new ReservationCompleteDetailResult(
            result.id(),
            result.shopName(),
            result.shopImageUrl(),
            LocalDateTime.of(result.reservationDate(), result.reservationTime()),
            result.partySize()
        );
    }

    @Override
    public ReservationDetailViewResult getReservationDetail(Long memberId, Long id) {
        ReservationDetailResult result = reservationQueryPort.findReservationDetailById(ReservationId.of(id))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
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
            result.status().name(),
            result.request(),
            result.createdAt()
        );
    }

    private void validateOwnership(Long ownerId, Long requesterId) {
        if (!Objects.equals(ownerId, requesterId)) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }
    }
}
