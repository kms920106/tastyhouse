package com.tastyhouse.core.domain.reservation.domain.model;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 가게의 특정 (날짜, 시간) 슬롯에 대한 예약 정원/점유 카운터.
 * 동시성/정원 제어의 핵심 애그리거트로, {@code @Version} 낙관적 락으로 동시 차감 충돌을 감지한다.
 * 정원 단위는 "예약 팀(건) 수"이다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
    name = "SHOP_RESERVATION_SLOT",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_shop_reservation_slot",
        columnNames = {"shop_id", "slot_date", "slot_time"}
    )
)
public class ShopReservationSlot extends BaseEntity {

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

    private ShopReservationSlot(Long shopId, LocalDate slotDate, LocalTime slotTime, Integer capacity) {
        this.shopId = shopId;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
        this.capacity = capacity != null ? capacity : ReservationSlot.CAPACITY_PER_SLOT;
        this.reservedCount = 0;
    }

    public static ShopReservationSlot of(Long shopId, LocalDate slotDate, LocalTime slotTime, Integer capacity) {
        return new ShopReservationSlot(shopId, slotDate, slotTime, capacity);
    }

    public boolean isFull() {
        return reservedCount >= capacity;
    }

    public int remaining() {
        return capacity - reservedCount;
    }

    /**
     * 정원 1팀 차감. 마감된 경우 예외. (정원 단위 = 팀 수)
     */
    public void reserve() {
        if (isFull()) {
            throw new BusinessException(ErrorCode.RESERVATION_SLOT_FULL);
        }
        this.reservedCount++;
    }

    /**
     * 정원 1팀 반납 (취소/거절 시).
     */
    public void release() {
        if (reservedCount > 0) {
            this.reservedCount--;
        }
    }
}
