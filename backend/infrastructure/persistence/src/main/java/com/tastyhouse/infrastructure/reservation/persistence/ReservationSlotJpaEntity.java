package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "RESERVATION_SLOT",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_reservation_slot",
        columnNames = {"shop_id", "slot_date", "slot_time"}
    )
)
public class ReservationSlotJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "reserved_count", nullable = false)
    private Integer reservedCount;

    @Version
    @Column(name = "version")
    private Long version;

    protected ReservationSlotJpaEntity() {
    }

    private ReservationSlotJpaEntity(Long shopId, LocalDate slotDate, LocalTime slotTime, Integer capacity, Integer reservedCount) {
        this.shopId = shopId;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
        this.capacity = capacity;
        this.reservedCount = reservedCount;
    }

    static ReservationSlotJpaEntity create(Long shopId, LocalDate slotDate, LocalTime slotTime, Integer capacity, Integer reservedCount) {
        return new ReservationSlotJpaEntity(shopId, slotDate, slotTime, capacity, reservedCount);
    }

    void applyChanges(Integer reservedCount) {
        this.reservedCount = reservedCount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public LocalDate getSlotDate() {
        return this.slotDate;
    }

    public LocalTime getSlotTime() {
        return this.slotTime;
    }

    public Integer getCapacity() {
        return this.capacity;
    }

    public Integer getReservedCount() {
        return this.reservedCount;
    }

    public Long getVersion() {
        return this.version;
    }
}
