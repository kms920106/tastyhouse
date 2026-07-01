package com.tastyhouse.core.domain.reservation.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.reservation.domain.model.Reservation;
import com.tastyhouse.core.domain.reservation.domain.model.ReservationStatus;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationRepository;

import static com.tastyhouse.core.domain.reservation.domain.model.QReservation.reservation;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JPAQueryFactory queryFactory;
    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public List<Reservation> findByMemberId(Long memberId) {
        return queryFactory.selectFrom(reservation)
            .where(reservation.memberId.eq(memberId))
            .orderBy(reservation.reservationDate.desc(), reservation.reservationTime.desc())
            .fetch();
    }

    @Override
    public List<Reservation> findByShopId(Long shopId) {
        return queryFactory.selectFrom(reservation)
            .where(reservation.shopId.eq(shopId))
            .orderBy(reservation.reservationDate.desc(), reservation.reservationTime.desc())
            .fetch();
    }

    @Override
    public boolean existsBlockingByMemberShopDate(Long memberId, Long shopId, LocalDate date) {
        return queryFactory.selectOne()
            .from(reservation)
            .where(
                reservation.memberId.eq(memberId),
                reservation.shopId.eq(shopId),
                reservation.reservationDate.eq(date),
                reservation.status.in(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED)
            )
            .fetchFirst() != null;
    }

    @Override
    public Optional<Reservation> findBlockingByMemberShopDate(Long memberId, Long shopId, LocalDate date) {
        return Optional.ofNullable(
            queryFactory.selectFrom(reservation)
                .where(
                    reservation.memberId.eq(memberId),
                    reservation.shopId.eq(shopId),
                    reservation.reservationDate.eq(date),
                    reservation.status.in(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED)
                )
                .orderBy(reservation.reservationTime.asc())
                .fetchFirst()
        );
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }
}
