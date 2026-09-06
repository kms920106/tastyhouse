package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.domain.reservation.model.ReservationStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "RESERVATION",
    indexes = {
        @Index(name = "idx_reservation_shop_slot", columnList = "shop_id, reservation_date, reservation_time"),
        @Index(name = "idx_reservation_member", columnList = "member_id")
    }
)
public class ReservationJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

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

    protected ReservationJpaEntity() {
    }

    private ReservationJpaEntity(
        Long memberId,
        Long shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        ReservationStatus status,
        String request
    ) {
        this.memberId = memberId;
        this.shopId = shopId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.status = status;
        this.request = request;
    }

    static ReservationJpaEntity create(
        Long memberId,
        Long shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        ReservationStatus status,
        String request
    ) {
        return new ReservationJpaEntity(memberId, shopId, reservationDate, reservationTime, partySize, status, request);
    }

    void applyChanges(ReservationStatus status) {
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public Long getShopId() {
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
}
