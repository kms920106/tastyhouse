package com.tastyhouse.core.domain.reservation.domain.repository;

import com.tastyhouse.core.domain.reservation.domain.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByShopId(Long shopId);

    /**
     * 동일 회원이 동일 가게의 동일 날짜에 재예약을 막는(PENDING/CONFIRMED/COMPLETED) 예약을 보유하고 있는지.
     * 회원당 1일 1예약 차단용. REJECTED/CANCELED는 제외.
     */
    boolean existsBlockingByMemberShopDate(Long memberId, Long shopId, LocalDate date);

    /**
     * 동일 회원이 동일 가게의 동일 날짜에 가진 차단 예약(PENDING/CONFIRMED/COMPLETED)의 시간 목록.
     * 가용성 조회 시 해당 슬롯을 비활성화하기 위해 사용.
     */
    List<LocalTime> findBlockingTimesByMemberShopDate(Long memberId, Long shopId, LocalDate date);

    Reservation save(Reservation reservation);
}
