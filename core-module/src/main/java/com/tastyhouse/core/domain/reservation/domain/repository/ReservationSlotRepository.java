package com.tastyhouse.core.domain.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.reservation.domain.model.ReservationSlot;

public interface ReservationSlotRepository {

    Optional<ReservationSlot> findByShopAndDateAndTime(Long shopId, LocalDate date, LocalTime time);

    List<ReservationSlot> findByShopAndDate(Long shopId, LocalDate date);

    ReservationSlot save(ReservationSlot slot);

    /**
     * 저장 직후 flush하여 유니크 제약·낙관적 락 충돌을 커밋 전 메서드 내부에서 즉시 노출한다.
     * 호출자의 재시도 루프가 해당 예외를 잡을 수 있어야 하는 동시성 경합 지점에서만 사용한다.
     */
    void saveAndFlush(ReservationSlot slot);
}
