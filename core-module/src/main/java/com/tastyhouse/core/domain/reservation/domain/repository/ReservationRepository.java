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
     * 동일 회원이 동일 가게의 동일 슬롯에 이미 활성(PENDING/CONFIRMED) 예약을 가지고 있는지.
     * 더블탭/중복 예약 차단용.
     */
    boolean existsActiveByMemberShopDateTime(Long memberId, Long shopId, LocalDate date, LocalTime time);

    Reservation save(Reservation reservation);
}
