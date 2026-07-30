package com.tastyhouse.core.domain.reservation.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;

import com.tastyhouse.core.domain.reservation.domain.service.SlotPolicy;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 가게의 특정 (날짜, 시간) 슬롯에 대한 예약 정원/점유 카운터 순수 도메인 모델.
 * 동시성/정원 제어의 핵심 애그리거트로, JPA {@code @Version} 낙관적 락으로 동시 차감 충돌을 감지한다.
 * 정원 단위는 "예약 팀(건) 수"이다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ReservationSlotJpaEntity} + {@code ReservationSlotMapper}가 담당한다.
 * 낙관적 락 버전({@code version})은 관리형(managed) 엔티티에서 flush 시 검증·증가되므로,
 * 이 POJO는 재구성 시 마지막으로 읽은 버전 값만 보관한다(직접 증가시키지 않음).
 */
@Getter
public class ReservationSlot {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long shopId; // 장소 ID
    private final LocalDate slotDate; // 슬롯 날짜
    private final LocalTime slotTime; // 슬롯 시간 (30분 단위)
    private final Integer capacity; // 슬롯당 정원 (팀 수)
    private Integer reservedCount; // 현재 점유 팀 수 (reserve/release로 재대입됨)
    private final Long version; // 낙관적 락 버전 (DB 재구성 시에만 값 존재)

    private ReservationSlot(
        Long id,
        Long shopId,
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

    /**
     * 신규 슬롯을 생성한다. 아직 영속되지 않았으므로 식별자·버전은 없다. capacity가 null이면 기본 정원을 사용한다.
     */
    public static ReservationSlot of(Long shopId, LocalDate slotDate, LocalTime slotTime, Integer capacity) {
        Integer resolvedCapacity = capacity != null ? capacity : SlotPolicy.CAPACITY_PER_SLOT;
        return new ReservationSlot(null, shopId, slotDate, slotTime, resolvedCapacity, 0, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·버전을 주입한다.
     */
    public static ReservationSlot reconstitute(
        Long id,
        Long shopId,
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
