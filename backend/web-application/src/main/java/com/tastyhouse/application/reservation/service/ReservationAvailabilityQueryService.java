package com.tastyhouse.application.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.reservation.model.ReservationStatus;
import com.tastyhouse.domain.reservation.model.SlotPolicy;
import com.tastyhouse.application.reservation.port.in.ReservationAvailabilityQueryUseCase;
import com.tastyhouse.application.reservation.port.out.ReservationQueryPort;
import com.tastyhouse.application.reservation.port.out.ReservationSlotAvailabilityResult;
import com.tastyhouse.application.reservation.port.out.ReservationSlotResult;
import com.tastyhouse.application.reservation.port.out.SlotOccupancyResult;

@Service
@Transactional(readOnly = true)
class ReservationAvailabilityQueryService implements ReservationAvailabilityQueryUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationQueryPort reservationQueryPort;

    public ReservationAvailabilityQueryService(ReservationQueryPort reservationQueryPort) {
        this.reservationQueryPort = reservationQueryPort;
    }

    @Override
    public ReservationSlotAvailabilityResult getAvailability(Long shopId, LocalDate date, Long memberId) {
        Map<LocalTime, Integer> remainingByTime = reservationQueryPort.findSlotOccupancies(shopId, date).stream()
            .collect(Collectors.toMap(SlotOccupancyResult::slotTime, SlotOccupancyResult::remaining));

        boolean hasMyReservation = reservationQueryPort.existsBlockingReservation(
            memberId, shopId, date, ReservationStatus.blockingStatuses().stream().map(ReservationStatus::name).toList());

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
}
