package com.tastyhouse.domain.reservation.repository;

import java.time.LocalDate;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ReservationRepository {
    Optional<Reservation> findById(ReservationId id);

    boolean existsBlockingByMemberShopDate(MemberId memberId, ShopId shopId, LocalDate date);

    Reservation save(Reservation reservation);
}
