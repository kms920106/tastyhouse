package com.tastyhouse.domain.reservation.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class Reservation {
    private final Long id;
    private final MemberId memberId;
    private final ShopId shopId;
    private final LocalDate reservationDate;
    private final LocalTime reservationTime;
    private final Integer partySize;
    private ReservationStatus status;
    private final String request;
    private final LocalDateTime createdAt;

    private Reservation(
        Long id,
        MemberId memberId,
        ShopId shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        ReservationStatus status,
        String request,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.shopId = shopId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.status = status;
        this.request = request;
        this.createdAt = createdAt;
    }

    public static Reservation of(
        MemberId memberId,
        ShopId shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        String request
    ) {
        if (partySize == null || partySize < 1) {
            throw new BusinessException(ErrorCode.RESERVATION_PARTY_SIZE_INVALID,
                ErrorCode.RESERVATION_PARTY_SIZE_INVALID.getDefaultMessage() + ": " + partySize);
        }

        return new Reservation(null, memberId, shopId, reservationDate, reservationTime, partySize,
            ReservationStatus.PENDING, request, null);
    }

    public static Reservation reconstitute(
        Long id,
        MemberId memberId,
        ShopId shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        ReservationStatus status,
        String request,
        LocalDateTime createdAt
    ) {
        return new Reservation(id, memberId, shopId, reservationDate, reservationTime, partySize, status, request, createdAt);
    }

    public ReservationId getReservationId() {
        return ReservationId.of(this.id);
    }

    public void validateOwnership(MemberId memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }
    }

    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void reject() {
        if (this.status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS);
        }
        this.status = ReservationStatus.REJECTED;
    }

    public void cancel() {
        switch (this.status) {
            case PENDING, CONFIRMED -> this.status = ReservationStatus.CANCELED;
            case CANCELED -> throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELED);
            case REJECTED -> throw new BusinessException(ErrorCode.RESERVATION_ALREADY_REJECTED);
            case COMPLETED -> throw new BusinessException(ErrorCode.RESERVATION_ALREADY_COMPLETED);
        }
    }

    public void complete() {
        if (this.status != ReservationStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS);
        }
        this.status = ReservationStatus.COMPLETED;
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public LocalDate getReservationDate() {
        return this.reservationDate;
    }

    public LocalTime getReservationTime() {
        return this.reservationTime;
    }

    public Integer getPartySize() {
        return this.partySize;
    }

    public ReservationStatus getStatus() {
        return this.status;
    }

    public String getRequest() {
        return this.request;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
