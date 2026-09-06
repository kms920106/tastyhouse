package com.tastyhouse.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.reservation.repository.ReservationRepository;
import com.tastyhouse.domain.reservation.repository.ReservationSlotRepository;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ShopOrderAvailabilityService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ReservationBookingService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository slotRepository;
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;
    private final ShopOrderAvailabilityService shopOrderAvailabilityService;

    public ReservationBookingService(
        ReservationRepository reservationRepository,
        ReservationSlotRepository slotRepository,
        ShopRepository shopRepository,
        MemberRepository memberRepository,
        ShopOrderAvailabilityService shopOrderAvailabilityService
    ) {
        this.reservationRepository = reservationRepository;
        this.slotRepository = slotRepository;
        this.shopRepository = shopRepository;
        this.memberRepository = memberRepository;
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
            throw new BusinessException(ErrorCode.RESERVATION_TERMS_NOT_AGREED);
        }

        if (!SlotPolicy.isValidSlot(time)) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_TIME);
        }

        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now(KST))) {
            throw new BusinessException(ErrorCode.RESERVATION_PAST_NOT_ALLOWED);
        }

        Shop shop = shopRepository.findVisibleById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (memberRepository.findById(memberId).isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        shopOrderAvailabilityService.validateOrderable(
            shop, OrderMethod.RESERVATION, LocalDateTime.of(date, time)
        );

        if (reservationRepository.existsBlockingByMemberShopDate(memberId, shopId, date)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        ReservationSlot slot = slotRepository
            .findByShopAndDateAndTime(shopId, date, time)
            .orElseGet(() -> ReservationSlot.of(shopId, date, time, SlotPolicy.CAPACITY_PER_SLOT));

        slot.reserve();
        slotRepository.saveAndFlush(slot);

        Reservation reservation = Reservation.of(memberId, shopId, date, time, partySize, request);
        Reservation saved = reservationRepository.save(reservation);

        return saved.getReservationId();
    }

    public void cancel(ReservationId reservationId, MemberId memberId) {
        Reservation reservation = getReservation(reservationId);
        reservation.validateOwnership(memberId);
        reservation.cancel();
        reservationRepository.save(reservation);
        releaseSlot(reservation);
    }

    public void reject(ReservationId reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.reject();
        reservationRepository.save(reservation);
        releaseSlot(reservation);
    }

    private Reservation getReservation(ReservationId reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void releaseSlot(Reservation reservation) {
        slotRepository
            .findByShopAndDateAndTime(
                reservation.getShopId(),
                reservation.getReservationDate(),
                reservation.getReservationTime()
            )
            .ifPresent(slot -> {
                slot.release();
                slotRepository.save(slot);
            });
    }
}
