package com.tastyhouse.domain.reservation.model;

import java.time.LocalDate;
import java.time.LocalTime;

import com.tastyhouse.domain.reservation.service.SlotPolicy;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class ReservationSlot {
    private final Long id;
    private final ShopId shopId;
    private final LocalDate slotDate;
    private final LocalTime slotTime;
    private final Integer capacity;
    private Integer reservedCount;
    private final Long version;

    private ReservationSlot(
        Long id,
        ShopId shopId,
        LocalDate slotDate,
        LocalTime slotTime,
        Integer capacity,
        Integer reservedCount,
        Long version
    ) {
        this.id = id;
        this.shopId = shopId;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
        this.capacity = capacity;
        this.reservedCount = reservedCount;
        this.version = version;
    }

    public static ReservationSlot of(ShopId shopId, LocalDate slotDate, LocalTime slotTime, Integer capacity) {
        Integer resolvedCapacity = capacity != null ? capacity : SlotPolicy.CAPACITY_PER_SLOT;
        return new ReservationSlot(null, shopId, slotDate, slotTime, resolvedCapacity, 0, null);
    }

    public static ReservationSlot reconstitute(
        Long id,
        ShopId shopId,
        LocalDate slotDate,
        LocalTime slotTime,
        Integer capacity,
        Integer reservedCount,
        Long version
    ) {
        return new ReservationSlot(id, shopId, slotDate, slotTime, capacity, reservedCount, version);
    }

    public boolean isFull() {
        return reservedCount >= capacity;
    }

    public int remaining() {
        return capacity - reservedCount;
    }

    public void reserve() {
        if (isFull()) {
            throw new BusinessException(ErrorCode.RESERVATION_SLOT_FULL);
        }
        this.reservedCount++;
    }

    public void release() {
        if (reservedCount > 0) {
            this.reservedCount--;
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
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
