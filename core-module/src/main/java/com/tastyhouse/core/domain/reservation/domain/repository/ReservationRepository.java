package com.tastyhouse.core.domain.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.reservation.domain.model.Reservation;

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
     * 동일 회원이 동일 가게의 동일 날짜에 가진 차단 예약(PENDING/CONFIRMED/COMPLETED) 1건.
     * 가용성 조회 시 "이 날짜에 내 예약이 있는지" 플래그와 기존 예약 ID 제공에 사용.
     * (회원+가게+날짜당 차단 예약은 최대 1건이므로 단건 조회)
     */
    Optional<Reservation> findBlockingByMemberShopDate(Long memberId, Long shopId, LocalDate date);

    Reservation save(Reservation reservation);
}
