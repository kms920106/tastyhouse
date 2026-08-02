package com.tastyhouse.domain.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 예약 슬롯 write 포트.
 *
 * <p>정원 차감·반납 불변식에 필요한 단건 로드와 저장만 둔다. 가용성 화면용 날짜별 슬롯 목록 조회는
 * infrastructure-module의 {@code ReservationQueryDao#findSlotOccupancies}가 담당한다.
 */
public interface ReservationSlotRepository {

    /**
     * 정원을 차감·반납할 슬롯 단건 로드(get-or-create의 get 측).
     */
    Optional<ReservationSlot> findByShopAndDateAndTime(ShopId shopId, LocalDate date, LocalTime time);

    ReservationSlot save(ReservationSlot slot);

    /**
     * 저장 직후 flush하여 유니크 제약·낙관적 락 충돌을 커밋 전 메서드 내부에서 즉시 노출한다.
     * 호출자의 재시도 루프가 해당 예외를 잡을 수 있어야 하는 동시성 경합 지점에서만 사용한다.
     */
    void saveAndFlush(ReservationSlot slot);
}
