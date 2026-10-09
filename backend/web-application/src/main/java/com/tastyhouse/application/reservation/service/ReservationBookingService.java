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
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.reservation.port.out.write.ReservationLoadPort;
import com.tastyhouse.application.reservation.port.out.write.ReservationSavePort;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotLoadPort;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.service.ShopOrderAvailabilityService;

@Service
public class ReservationBookingService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationLoadPort reservationLoadPort;
    private final ReservationSavePort reservationSavePort;
    private final ReservationSlotLoadPort slotLoadPort;
    private final ReservationSlotSavePort slotSavePort;
    private final ShopLoadPort shopLoadPort;
    private final MemberLoadPort memberLoadPort;
    private final ShopOrderAvailabilityService shopOrderAvailabilityService;

    public ReservationBookingService(
        ReservationLoadPort reservationLoadPort,
        ReservationSavePort reservationSavePort,
        ReservationSlotLoadPort slotLoadPort,
        ReservationSlotSavePort slotSavePort,
        ShopLoadPort shopLoadPort,
        MemberLoadPort memberLoadPort,
        ShopOrderAvailabilityService shopOrderAvailabilityService
    ) {
        this.reservationLoadPort = reservationLoadPort;
        this.reservationSavePort = reservationSavePort;
        this.slotLoadPort = slotLoadPort;
        this.slotSavePort = slotSavePort;
        this.shopLoadPort = shopLoadPort;
        this.memberLoadPort = memberLoadPort;
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

        Shop shop = shopLoadPort.findVisibleById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
        if (memberLoadPort.findById(memberId).isEmpty()) {
            throw new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND);
        }

        shopOrderAvailabilityService.validateOrderable(
            shop, OrderMethod.RESERVATION, LocalDateTime.of(date, time)
        );

        if (reservationLoadPort.existsBlockingByMemberShopDate(memberId, shopId, date)) {
            throw new ApplicationException(WebErrorCode.DUPLICATE_RESERVATION);
        }

        ReservationSlot slot = slotLoadPort
            .findByShopAndDateAndTime(shopId, date, time)
            .orElseGet(() -> ReservationSlot.of(shopId, date, time, SlotPolicy.CAPACITY_PER_SLOT));

        slot.reserve();
        slotSavePort.saveImmediately(slot);

        Reservation reservation = Reservation.of(memberId, shopId, date, time, partySize, request);
        Reservation saved = reservationSavePort.save(reservation);

        return saved.getReservationId();
    }

    public void cancel(ReservationId reservationId, MemberId memberId) {
        Reservation reservation = getReservation(reservationId);
        reservation.validateOwnership(memberId);
        reservation.cancel();
        reservationSavePort.save(reservation);
        releaseSlot(reservation);
    }

    public void reject(ReservationId reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.reject();
        reservationSavePort.save(reservation);
        releaseSlot(reservation);
    }

    private Reservation getReservation(ReservationId reservationId) {
        return reservationLoadPort.findById(reservationId)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.RESERVATION_NOT_FOUND));
    }

    private void releaseSlot(Reservation reservation) {
        slotLoadPort
            .findByShopAndDateAndTime(
                reservation.getShopId(),
                reservation.getReservationDate(),
                reservation.getReservationTime()
            )
            .ifPresent(slot -> {
                slot.release();
                slotSavePort.save(slot);
            });
    }
}
