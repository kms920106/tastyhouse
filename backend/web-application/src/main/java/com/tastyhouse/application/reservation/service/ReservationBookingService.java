package com.tastyhouse.application.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.reservation.model.SlotPolicy;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.reservation.port.out.write.ReservationPersistencePort;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;
import com.tastyhouse.application.shop.service.ShopOrderAvailabilityService;

@Service
public class ReservationBookingService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationPersistencePort reservationPersistencePort;
    private final ReservationSlotPersistencePort slotPersistencePort;
    private final ShopPersistencePort shopPersistencePort;
    private final MemberPersistencePort memberPersistencePort;
    private final ShopOrderAvailabilityService shopOrderAvailabilityService;

    public ReservationBookingService(
        ReservationPersistencePort reservationPersistencePort,
        ReservationSlotPersistencePort slotPersistencePort,
        ShopPersistencePort shopPersistencePort,
        MemberPersistencePort memberPersistencePort,
        ShopOrderAvailabilityService shopOrderAvailabilityService
    ) {
        this.reservationPersistencePort = reservationPersistencePort;
        this.slotPersistencePort = slotPersistencePort;
        this.shopPersistencePort = shopPersistencePort;
        this.memberPersistencePort = memberPersistencePort;
        this.shopOrderAvailabilityService = shopOrderAvailabilityService;
    }

    public ReservationId book(
        MemberId memberId,
        ShopId shopId,
        LocalDate date,
        LocalTime time,
        Integer partySize,
        String request,
        boolean agreedRequiredTerms
    ) {
        if (!agreedRequiredTerms) {
            throw new ApplicationException(WebErrorCode.RESERVATION_TERMS_NOT_AGREED);
        }

        if (!SlotPolicy.isValidSlot(time)) {
            throw new ApplicationException(WebErrorCode.RESERVATION_INVALID_TIME);
        }

        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now(KST))) {
            throw new ApplicationException(WebErrorCode.RESERVATION_PAST_NOT_ALLOWED);
        }

        Shop shop = shopPersistencePort.findVisibleById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
        if (memberPersistencePort.findById(memberId).isEmpty()) {
            throw new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND);
        }

        shopOrderAvailabilityService.validateOrderable(
            shop, OrderMethod.RESERVATION, LocalDateTime.of(date, time)
        );

        if (reservationPersistencePort.existsBlockingByMemberShopDate(memberId, shopId, date)) {
            throw new ApplicationException(WebErrorCode.DUPLICATE_RESERVATION);
        }

        ReservationSlot slot = slotPersistencePort
            .findByShopAndDateAndTime(shopId, date, time)
            .orElseGet(() -> ReservationSlot.of(shopId, date, time, SlotPolicy.CAPACITY_PER_SLOT));

        slot.reserve();
        slotPersistencePort.saveImmediately(slot);

        Reservation reservation = Reservation.of(memberId, shopId, date, time, partySize, request);
        Reservation saved = reservationPersistencePort.save(reservation);

        return saved.getReservationId();
    }

    public void cancel(ReservationId reservationId, MemberId memberId) {
        Reservation reservation = getReservation(reservationId);
        reservation.validateOwnership(memberId);
        reservation.cancel();
        reservationPersistencePort.save(reservation);
        releaseSlot(reservation);
    }

    public void reject(ReservationId reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.reject();
        reservationPersistencePort.save(reservation);
        releaseSlot(reservation);
    }

    private Reservation getReservation(ReservationId reservationId) {
        return reservationPersistencePort.findById(reservationId)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.RESERVATION_NOT_FOUND));
    }

    private void releaseSlot(Reservation reservation) {
        slotPersistencePort
            .findByShopAndDateAndTime(
                reservation.getShopId(),
                reservation.getReservationDate(),
                reservation.getReservationTime()
            )
            .ifPresent(slot -> {
                slot.release();
                slotPersistencePort.save(slot);
            });
    }
}
