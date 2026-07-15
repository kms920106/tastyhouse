package com.tastyhouse.core.domain.reservation.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.reservation.domain.vo.ReservationId;
import com.tastyhouse.core.domain.member.infrastructure.persistence.converter.MemberIdConverter;
import com.tastyhouse.core.exception.AccessDeniedException;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
    name = "RESERVATION",
    indexes = {
        @Index(name = "idx_reservation_shop_slot", columnList = "shop_id, reservation_date, reservation_time"),
        @Index(name = "idx_reservation_member", columnList = "member_id")
    }
)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReservationStatus status;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    private Reservation(
        MemberId memberId,
        Long shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        String request
    ) {
        this.memberId = memberId;
        this.shopId = shopId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.request = request;
        this.status = ReservationStatus.PENDING;
    }

    public static Reservation of(
        MemberId memberId,
        Long shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        String request
    ) {
        return new Reservation(memberId, shopId, reservationDate, reservationTime, partySize, request);
    }

    public ReservationId getReservationId() {
        return ReservationId.of(this.id);
    }

    /**
     * 예약자 본인 검증. 본인이 아니면 접근 거부.
     */
    public void validateOwnership(MemberId memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }
    }

    /**
     * 점주 승인: PENDING -> CONFIRMED
     */
    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    /**
     * 점주 거절: PENDING -> REJECTED
     */
    public void reject() {
        if (this.status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS);
        }
        this.status = ReservationStatus.REJECTED;
    }

    /**
     * 사용자 취소: PENDING|CONFIRMED -> CANCELED
     * 취소 불가 상태는 사유별로 구분된 예외를 던져 안내 메시지를 세분화한다.
     */
    public void cancel() {
        switch (this.status) {
            case PENDING, CONFIRMED -> this.status = ReservationStatus.CANCELED;
            case CANCELED -> throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELED);
            case REJECTED -> throw new BusinessException(ErrorCode.RESERVATION_ALREADY_REJECTED);
            case COMPLETED -> throw new BusinessException(ErrorCode.RESERVATION_ALREADY_COMPLETED);
        }
    }

    /**
     * 방문 완료: CONFIRMED -> COMPLETED
     */
    public void complete() {
        if (this.status != ReservationStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS);
        }
        this.status = ReservationStatus.COMPLETED;
    }
}
